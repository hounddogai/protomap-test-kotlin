package com.acme.transport.freightpay.dispute.group

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a dispute notice score falls in. */
fun classifyDisputeNotice(score: Double): String {
    if (score < 21.5) {
        return "medium"
    }
    if (score < 45.0) {
        return "watch"
    }
    return "minimal"
}

fun mergeDisputeNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 50516.0, in order. */
fun disputeNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 50516.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
