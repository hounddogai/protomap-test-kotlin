package com.acme.transport.freightpay.fuelsurcharge.adjustment

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFuelSurchargeQuota(): Int {
    val policy = defaultPolicy("fuel-surcharge-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a fuel surcharge quota code in its canonical FSQ- form. */
fun normalizeFuelSurchargeQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FSQ-")) {
        return clean
    }
    return "FSQ-${clean.replace(" ", "-")}"
}

fun fuelSurchargeQuotaBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
