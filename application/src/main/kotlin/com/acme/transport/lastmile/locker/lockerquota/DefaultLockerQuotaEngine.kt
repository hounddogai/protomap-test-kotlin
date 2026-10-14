package com.acme.transport.lastmile.locker.lockerquota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default locker quota calculation. */
interface DefaultLockerQuotaFactor {
    fun describe(): String
    fun adjust(item: DefaultLockerQuota, value: Double): Double
}

data class FlaggedDefaultLockerQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultLockerQuotaFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultLockerQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDefaultLockerQuotaAdjuster(var percent: Double) : DefaultLockerQuotaFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultLockerQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDefaultLockerQuotaAdjuster(var step: Double) : DefaultLockerQuotaFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultLockerQuota, value: Double): Double = roundTo(value, step)
}

/** Runs the default locker quota steps in order and keeps a trace of each. */
data class DefaultLockerQuotaEngine(
    var steps: MutableList<DefaultLockerQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultLockerQuota): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultLockerQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultLockerQuotaEngine(flags: Registry): DefaultLockerQuotaEngine {
    return DefaultLockerQuotaEngine(
        steps = mutableListOf<DefaultLockerQuotaFactor>(
            FlaggedDefaultLockerQuotaAdjuster(flags, "default-locker-quota-weighted", -5.0),
            PercentDefaultLockerQuotaAdjuster(-10.2),
            RoundingDefaultLockerQuotaAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 26227.8,
    )
}
