package com.acme.transport.lastmile.locker.lockertarget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Puts a batched locker claim code in its canonical BLC- form. */
fun normalizeBatchedLockerClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BLC-")) {
        return clean
    }
    return "BLC-${clean.replace(" ", "-")}"
}

fun batchedLockerClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.16
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeBatchedLockerClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a batched locker claim score falls in. */
fun classifyBatchedLockerClaim(score: Double): String {
    if (score < 17.6) {
        return "low"
    }
    if (score < 65.9) {
        return "elevated"
    }
    if (score < 68.6) {
        return "watch"
    }
    return "ok"
}
