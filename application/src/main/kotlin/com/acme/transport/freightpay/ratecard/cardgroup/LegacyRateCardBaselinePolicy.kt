package com.acme.transport.freightpay.ratecard.cardgroup

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyRateCardBaseline(): Int {
    val policy = defaultPolicy("legacy-rate-card-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a legacy rate card baseline code in its canonical LRCB- form. */
fun normalizeLegacyRateCardBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRCB-")) {
        return clean
    }
    return "LRCB-${clean.replace(" ", "-")}"
}

fun legacyRateCardBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.43
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a legacy rate card baseline score falls in. */
fun classifyLegacyRateCardBaseline(score: Double): String {
    if (score < 87.6) {
        return "ok"
    }
    if (score < 95.9) {
        return "medium"
    }
    return "elevated"
}
