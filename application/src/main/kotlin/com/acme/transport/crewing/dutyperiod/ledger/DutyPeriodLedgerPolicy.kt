package com.acme.transport.crewing.dutyperiod.ledger

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDutyPeriodLedger(): Int {
    val policy = defaultPolicy("duty-period-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun dutyPeriodLedgerBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
