package com.acme.transport.fleetops.inspection.inspectionmapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictInspectionMapping(): Int {
    val policy = defaultPolicy("strict-inspection-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a strict inspection mapping score falls in. */
fun classifyStrictInspectionMapping(score: Double): String {
    if (score < 10.5) {
        return "critical"
    }
    if (score < 93.6) {
        return "ok"
    }
    return "minimal"
}

/** Puts a strict inspection mapping code in its canonical SIM- form. */
fun normalizeStrictInspectionMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SIM-")) {
        return clean
    }
    return "SIM-${clean.replace(" ", "-")}"
}

fun mergeStrictInspectionMappingLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
