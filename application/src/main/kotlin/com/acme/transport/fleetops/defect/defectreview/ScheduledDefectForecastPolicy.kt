package com.acme.transport.fleetops.defect.defectreview

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeScheduledDefectForecastLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledDefectForecast(): Int {
    val policy = defaultPolicy("scheduled-defect-forecast")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a scheduled defect forecast code in its canonical SDF- form. */
fun normalizeScheduledDefectForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDF-")) {
        return clean
    }
    return "SDF-${clean.replace(" ", "-")}"
}
