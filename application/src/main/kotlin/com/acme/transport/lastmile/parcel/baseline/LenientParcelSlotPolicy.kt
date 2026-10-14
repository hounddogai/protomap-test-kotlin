package com.acme.transport.lastmile.parcel.baseline

import com.acme.transport.kit.retry.defaultPolicy

fun lenientParcelSlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientParcelSlot(): Int {
    val policy = defaultPolicy("lenient-parcel-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 54405.0, in order. */
fun lenientParcelSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 54405.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
