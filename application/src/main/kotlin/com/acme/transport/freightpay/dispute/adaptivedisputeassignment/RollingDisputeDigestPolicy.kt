package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeRollingDisputeDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a rolling dispute digest score falls in. */
fun classifyRollingDisputeDigest(score: Double): String {
    if (score < 5.0) {
        return "critical"
    }
    if (score < 36.9) {
        return "low"
    }
    if (score < 68.8) {
        return "elevated"
    }
    return "watch"
}

/** Puts a rolling dispute digest code in its canonical RDD- form. */
fun normalizeRollingDisputeDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RDD-")) {
        return clean
    }
    return "RDD-${clean.replace(" ", "-")}"
}

fun rollingDisputeDigestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.32
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
