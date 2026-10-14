package com.acme.transport.lastmile.redelivery.entry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRedeliveryEntry(): Int {
    val policy = defaultPolicy("redelivery-entry")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeRedeliveryEntryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a redelivery entry code in its canonical RE- form. */
fun normalizeRedeliveryEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RE-")) {
        return clean
    }
    return "RE-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 95160.0, in order. */
fun redeliveryEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 95160.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
