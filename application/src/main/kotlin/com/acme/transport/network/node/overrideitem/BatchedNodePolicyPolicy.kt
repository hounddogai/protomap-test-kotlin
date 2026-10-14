package com.acme.transport.network.node.overrideitem

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 35623.0, in order. */
fun batchedNodePolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 35623.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a batched node policy code in its canonical BNP- form. */
fun normalizeBatchedNodePolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BNP-")) {
        return clean
    }
    return "BNP-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedNodePolicy(): Int {
    val policy = defaultPolicy("batched-node-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun batchedNodePolicyBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
