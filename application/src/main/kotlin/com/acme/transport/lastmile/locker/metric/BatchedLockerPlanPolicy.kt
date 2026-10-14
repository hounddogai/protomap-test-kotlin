package com.acme.transport.lastmile.locker.metric

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a batched locker plan score falls in. */
fun classifyBatchedLockerPlan(score: Double): String {
    if (score < 65.5) {
        return "low"
    }
    if (score < 72.0) {
        return "medium"
    }
    return "minimal"
}

/** Puts a batched locker plan code in its canonical BLP- form. */
fun normalizeBatchedLockerPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BLP-")) {
        return clean
    }
    return "BLP-${clean.replace(" ", "-")}"
}

fun mergeBatchedLockerPlanLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun batchedLockerPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
