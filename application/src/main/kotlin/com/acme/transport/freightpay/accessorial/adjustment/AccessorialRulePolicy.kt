package com.acme.transport.freightpay.accessorial.adjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun accessorialRuleBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a accessorial rule score falls in. */
fun classifyAccessorialRule(score: Double): String {
    if (score < 5.0) {
        return "minimal"
    }
    if (score < 12.4) {
        return "ok"
    }
    if (score < 72.5) {
        return "low"
    }
    if (score < 76.1) {
        return "watch"
    }
    return "high"
}

fun mergeAccessorialRuleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun accessorialRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
