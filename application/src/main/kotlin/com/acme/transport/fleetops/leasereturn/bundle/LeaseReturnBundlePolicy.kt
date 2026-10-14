package com.acme.transport.fleetops.leasereturn.bundle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeLeaseReturnBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a lease return bundle score falls in. */
fun classifyLeaseReturnBundle(score: Double): String {
    if (score < 8.9) {
        return "minimal"
    }
    if (score < 18.4) {
        return "high"
    }
    if (score < 41.9) {
        return "watch"
    }
    if (score < 97.8) {
        return "ok"
    }
    return "elevated"
}

fun leaseReturnBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
