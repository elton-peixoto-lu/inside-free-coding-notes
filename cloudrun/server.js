const express = require("express");
const { Storage } = require("@google-cloud/storage");

const app = express();
const storage = new Storage();
const bucketName = process.env.SITE_BUCKET;
const port = process.env.PORT || 8080;

if (!bucketName) {
  throw new Error("SITE_BUCKET env var is required");
}

const bucket = storage.bucket(bucketName);

const MIME = {
  ".html": "text/html; charset=utf-8",
  ".css": "text/css; charset=utf-8",
  ".js": "application/javascript; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".map": "application/json; charset=utf-8",
  ".png": "image/png",
  ".jpg": "image/jpeg",
  ".jpeg": "image/jpeg",
  ".svg": "image/svg+xml",
  ".ico": "image/x-icon",
  ".edn": "application/edn; charset=utf-8"
};

function contentType(path) {
  const ext = path.slice(path.lastIndexOf("."));
  return MIME[ext] || "application/octet-stream";
}

async function sendObject(res, objectPath) {
  const file = bucket.file(objectPath);
  const [exists] = await file.exists();
  if (!exists) {
    return false;
  }

  res.setHeader("Content-Type", contentType(objectPath));
  file.createReadStream().on("error", () => {
    if (!res.headersSent) {
      res.status(500).send("internal error");
    }
  }).pipe(res);
  return true;
}

app.get("*", async (req, res) => {
  try {
    const rawPath = decodeURIComponent(req.path || "/");
    const isRoot = rawPath === "/" || rawPath === "";
    const objectPath = isRoot ? "index.html" : rawPath.replace(/^\/+/, "");

    const served = await sendObject(res, objectPath);
    if (served) return;

    const fallback = await sendObject(res, "index.html");
    if (!fallback) {
      res.status(404).send("not found");
    }
  } catch (err) {
    res.status(500).send("internal error");
  }
});

app.listen(port, () => {
  console.log(`listening on :${port}`);
});
