package com.acme.transport.customs.declaration.policy

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun tieredDeclarationPolicyBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a tiered declaration policy code in its canonical TDP- form. */
fun normalizeTieredDeclarationPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TDP-")) {
        return clean
    }
    return "TDP-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredDeclarationPolicy(): Int {
    val policy = defaultPolicy("tiered-declaration-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 22857.0, in order. */
fun tieredDeclarationPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 22857.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
