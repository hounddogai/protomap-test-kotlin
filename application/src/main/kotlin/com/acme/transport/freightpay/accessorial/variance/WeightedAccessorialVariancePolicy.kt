package com.acme.transport.freightpay.accessorial.variance

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun weightedAccessorialVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.84
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedAccessorialVariance(): Int {
    val policy = defaultPolicy("weighted-accessorial-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a weighted accessorial variance code in its canonical WAV- form. */
fun normalizeWeightedAccessorialVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WAV-")) {
        return clean
    }
    return "WAV-${clean.replace(" ", "-")}"
}
