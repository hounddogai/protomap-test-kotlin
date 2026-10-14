package com.acme.transport.lastmile.locker.lockerrule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Puts a scheduled locker rule code in its canonical SLR- form. */
fun normalizeScheduledLockerRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SLR-")) {
        return clean
    }
    return "SLR-${clean.replace(" ", "-")}"
}

fun scheduledLockerRuleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun mergeScheduledLockerRuleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a scheduled locker rule score falls in. */
fun classifyScheduledLockerRule(score: Double): String {
    if (score < 36.1) {
        return "critical"
    }
    if (score < 55.3) {
        return "high"
    }
    if (score < 87.7) {
        return "watch"
    }
    return "low"
}
