package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingDutyPeriodBudget(): Int {
    val policy = defaultPolicy("pending-duty-period-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergePendingDutyPeriodBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 98653.0, in order. */
fun pendingDutyPeriodBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 98653.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a pending duty period budget code in its canonical PDPB- form. */
fun normalizePendingDutyPeriodBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PDPB-")) {
        return clean
    }
    return "PDPB-${clean.replace(" ", "-")}"
}
