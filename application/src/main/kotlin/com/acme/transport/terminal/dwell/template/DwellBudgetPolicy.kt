package com.acme.transport.terminal.dwell.template

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDwellBudget(): Int {
    val policy = defaultPolicy("dwell-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a dwell budget code in its canonical DB- form. */
fun normalizeDwellBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DB-")) {
        return clean
    }
    return "DB-${clean.replace(" ", "-")}"
}
