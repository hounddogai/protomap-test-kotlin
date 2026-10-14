package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a duty period baseline code in its canonical DPB- form. */
fun normalizeDutyPeriodBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DPB-")) {
        return clean
    }
    return "DPB-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDutyPeriodBaseline(): Int {
    val policy = defaultPolicy("duty-period-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 75838.0, in order. */
fun dutyPeriodBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 75838.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
