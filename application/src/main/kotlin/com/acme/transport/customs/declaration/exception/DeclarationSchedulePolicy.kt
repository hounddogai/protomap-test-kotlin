package com.acme.transport.customs.declaration.exception

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDeclarationSchedule(): Int {
    val policy = defaultPolicy("declaration-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 94821.0, in order. */
fun declarationScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 94821.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a declaration schedule code in its canonical DS- form. */
fun normalizeDeclarationScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DS-")) {
        return clean
    }
    return "DS-${clean.replace(" ", "-")}"
}
