package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a adaptive inspection draft code in its canonical AID- form. */
fun normalizeAdaptiveInspectionDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AID-")) {
        return clean
    }
    return "AID-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 31158.0, in order. */
fun adaptiveInspectionDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 31158.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveInspectionDraft(): Int {
    val policy = defaultPolicy("adaptive-inspection-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun adaptiveInspectionDraftBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
