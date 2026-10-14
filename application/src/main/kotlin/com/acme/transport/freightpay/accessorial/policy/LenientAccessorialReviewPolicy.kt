package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun lenientAccessorialReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 31432.0, in order. */
fun lenientAccessorialReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 31432.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeLenientAccessorialReviewLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a lenient accessorial review score falls in. */
fun classifyLenientAccessorialReview(score: Double): String {
    if (score < 21.6) {
        return "low"
    }
    if (score < 42.1) {
        return "watch"
    }
    if (score < 63.3) {
        return "ok"
    }
    if (score < 99.4) {
        return "medium"
    }
    return "high"
}
