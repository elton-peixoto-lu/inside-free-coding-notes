function asObject(v) {
  return v && typeof v === "object" ? v : {};
}

function keysUnion(a, b) {
  return Array.from(new Set([...Object.keys(asObject(a)), ...Object.keys(asObject(b))]));
}

function scoreDrift(desired, observed) {
  const keys = keysUnion(desired, observed);
  if (keys.length === 0) return 0;
  let mismatches = 0;
  for (const k of keys) {
    const d = desired?.[k];
    const o = observed?.[k];
    if (typeof d === "number" && typeof o === "number") {
      const denom = Math.max(1, Math.abs(d));
      if (Math.abs(d - o) / denom > 0.02) mismatches += 1;
    } else if (JSON.stringify(d) !== JSON.stringify(o)) {
      mismatches += 1;
    }
  }
  return Math.min(1, mismatches / keys.length);
}

function severity(score) {
  if (score <= 0.2) return "healthy";
  if (score <= 0.5) return "attention";
  if (score <= 0.8) return "high";
  return "critical";
}

function nextValue(desired, observed, depFactor, tick) {
  if (typeof desired === "number" && typeof observed === "number") {
    const delta = desired - observed;
    if (Math.abs(delta) < 0.02) return desired;
    return observed + delta * (0.22 + 0.22 * depFactor);
  }
  if (JSON.stringify(desired) === JSON.stringify(observed)) {
    // Runtime noise: occasionally create small drift on running systems.
    if (typeof observed === "number" && tick % 11 === 0 && Math.random() < 0.25) {
      return observed + (Math.random() < 0.5 ? -1 : 1);
    }
    return observed;
  }
  const chance = 0.12 + 0.28 * depFactor + (tick % 5 === 0 ? 0.2 : 0);
  return Math.random() < chance ? desired : observed;
}

export function reconcileStep(nodes, edges, tick) {
  const byId = new Map(nodes.map((n) => [n.id, n]));
  const inMap = new Map(nodes.map((n) => [n.id, []]));
  for (const e of edges) {
    if (inMap.has(e.target)) inMap.get(e.target).push(e.source);
  }

  const timelineEntries = [];
  const nextNodes = nodes.map((node) => {
    const deps = inMap.get(node.id) || [];
    const depDrift =
      deps.length === 0
        ? 0
        : deps.reduce((acc, id) => acc + (byId.get(id)?.driftScore ?? byId.get(id)?.["drift-score"] ?? 0), 0) / deps.length;
    const depFactor = Math.max(0.35, 1 - depDrift * 0.6);

    const desired = asObject(node.desired);
    const observed = { ...asObject(node.observed) };
    const changedKeys = [];

    for (const k of keysUnion(desired, observed)) {
      const before = observed[k];
      const after = nextValue(desired[k], observed[k], depFactor, tick);
      if (JSON.stringify(before) !== JSON.stringify(after)) {
        observed[k] = after;
        changedKeys.push(k);
      }
    }

    const driftScore = scoreDrift(desired, observed);
    const next = {
      ...node,
      observed,
      driftScore,
      drift_score: driftScore,
      severity: severity(driftScore),
      status: severity(driftScore),
    };

    if (changedKeys.length > 0) {
      timelineEntries.push({
        id: `${node.id}-${tick}`,
        tick,
        nodeId: node.id,
        label: node.label,
        changedKeys,
        driftScore,
      });
    }
    return next;
  });

  if (tick % 8 === 0) {
    timelineEntries.unshift({
      id: `scan-${tick}`,
      tick,
      nodeId: "controller",
      label: "Reconcile Controller",
      changedKeys: ["scan"],
      driftScore: nextNodes.reduce((a, n) => a + (n.driftScore ?? n["drift-score"] ?? 0), 0) / Math.max(1, nextNodes.length),
    });
  }

  return { nodes: nextNodes, timelineEntries };
}
