package com.acme.transport.linehaul.relay.review

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeLenientRelayNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientRelayNotice(): Int {
    val policy = defaultPolicy("lenient-relay-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 17651.0, in order. */
fun lenientRelayNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 17651.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
