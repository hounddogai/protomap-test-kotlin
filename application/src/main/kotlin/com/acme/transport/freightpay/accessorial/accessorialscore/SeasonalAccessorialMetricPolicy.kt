package com.acme.transport.freightpay.accessorial.accessorialscore

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Puts a seasonal accessorial metric code in its canonical SAM- form. */
fun normalizeSeasonalAccessorialMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAM-")) {
        return clean
    }
    return "SAM-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalAccessorialMetric(): Int {
    val policy = defaultPolicy("seasonal-accessorial-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a seasonal accessorial metric score falls in. */
fun classifySeasonalAccessorialMetric(score: Double): String {
    if (score < 41.5) {
        return "low"
    }
    if (score < 44.5) {
        return "ok"
    }
    if (score < 72.3) {
        return "watch"
    }
    return "high"
}

fun mergeSeasonalAccessorialMetricLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
