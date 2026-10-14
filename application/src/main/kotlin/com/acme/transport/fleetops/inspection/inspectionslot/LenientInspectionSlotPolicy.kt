package com.acme.transport.fleetops.inspection.inspectionslot

import com.acme.transport.kit.retry.defaultPolicy

fun lenientInspectionSlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientInspectionSlot(): Int {
    val policy = defaultPolicy("lenient-inspection-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 78804.0, in order. */
fun lenientInspectionSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 78804.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
