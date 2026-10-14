package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a batched duty period agreement code in its canonical BDPA- form. */
fun normalizeBatchedDutyPeriodAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BDPA-")) {
        return clean
    }
    return "BDPA-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedDutyPeriodAgreement(): Int {
    val policy = defaultPolicy("batched-duty-period-agreement")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
