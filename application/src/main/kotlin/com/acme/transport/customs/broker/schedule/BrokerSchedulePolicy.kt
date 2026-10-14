package com.acme.transport.customs.broker.schedule

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a broker schedule code in its canonical BS- form. */
fun normalizeBrokerScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BS-")) {
        return clean
    }
    return "BS-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBrokerSchedule(): Int {
    val policy = defaultPolicy("broker-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
