package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a adaptive tariff slot code in its canonical ATS- form. */
fun normalizeAdaptiveTariffSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ATS-")) {
        return clean
    }
    return "ATS-${clean.replace(" ", "-")}"
}

/** Names the band a adaptive tariff slot score falls in. */
fun classifyAdaptiveTariffSlot(score: Double): String {
    if (score < 11.5) {
        return "minimal"
    }
    if (score < 58.8) {
        return "low"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveTariffSlot(): Int {
    val policy = defaultPolicy("adaptive-tariff-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun adaptiveTariffSlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.74
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
