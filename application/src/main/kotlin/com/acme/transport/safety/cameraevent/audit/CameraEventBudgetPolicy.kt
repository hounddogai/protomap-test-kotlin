package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a camera event budget score falls in. */
fun classifyCameraEventBudget(score: Double): String {
    if (score < 87.0) {
        return "medium"
    }
    if (score < 92.4) {
        return "minimal"
    }
    if (score < 97.9) {
        return "ok"
    }
    return "watch"
}

fun cameraEventBudgetBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a camera event budget code in its canonical CEB- form. */
fun normalizeCameraEventBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CEB-")) {
        return clean
    }
    return "CEB-${clean.replace(" ", "-")}"
}

fun mergeCameraEventBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
