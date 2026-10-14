package com.acme.transport.customs.tariff.tariffschedule

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Counts how many amounts fit in a budget of 72686.0, in order. */
fun fallbackTariffScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 72686.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun fallbackTariffScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.44
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackTariffSchedule(): Int {
    val policy = defaultPolicy("fallback-tariff-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a fallback tariff schedule score falls in. */
fun classifyFallbackTariffSchedule(score: Double): String {
    if (score < 8.3) {
        return "elevated"
    }
    if (score < 9.5) {
        return "low"
    }
    return "high"
}
