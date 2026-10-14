package com.acme.transport.linehaul.laneplan.summary

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun batchedLanePlanBundleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a batched lane plan bundle code in its canonical BLPB- form. */
fun normalizeBatchedLanePlanBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BLPB-")) {
        return clean
    }
    return "BLPB-${clean.replace(" ", "-")}"
}

fun batchedLanePlanBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

fun batchedLanePlanBundleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.66
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
