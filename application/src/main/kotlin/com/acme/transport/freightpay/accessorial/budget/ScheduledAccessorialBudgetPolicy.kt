package com.acme.transport.freightpay.accessorial.budget

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a scheduled accessorial budget code in its canonical SAB- form. */
fun normalizeScheduledAccessorialBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAB-")) {
        return clean
    }
    return "SAB-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledAccessorialBudget(): Int {
    val policy = defaultPolicy("scheduled-accessorial-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
