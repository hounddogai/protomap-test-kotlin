package com.acme.transport.freightpay.fuelsurcharge.baseline

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a rolling fuel surcharge ledger code in its canonical RFSL- form. */
fun normalizeRollingFuelSurchargeLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RFSL-")) {
        return clean
    }
    return "RFSL-${clean.replace(" ", "-")}"
}

/** Names the band a rolling fuel surcharge ledger score falls in. */
fun classifyRollingFuelSurchargeLedger(score: Double): String {
    if (score < 33.1) {
        return "ok"
    }
    if (score < 63.0) {
        return "elevated"
    }
    if (score < 67.1) {
        return "critical"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingFuelSurchargeLedger(): Int {
    val policy = defaultPolicy("rolling-fuel-surcharge-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
