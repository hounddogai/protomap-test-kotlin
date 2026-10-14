package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a service plan snapshot score falls in. */
fun classifyServicePlanSnapshot(score: Double): String {
    if (score < 66.1) {
        return "ok"
    }
    if (score < 75.8) {
        return "critical"
    }
    if (score < 81.7) {
        return "minimal"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeServicePlanSnapshot(): Int {
    val policy = defaultPolicy("service-plan-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeServicePlanSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a service plan snapshot code in its canonical SPS- form. */
fun normalizeServicePlanSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPS-")) {
        return clean
    }
    return "SPS-${clean.replace(" ", "-")}"
}
