package com.acme.transport.customs.tariff.tariffprofile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling tariff threshold calculation. */
interface RollingTariffThresholdModifier {
    fun describe(): String
    fun adjust(item: RollingTariffThreshold, value: Double): Double
}

data class RoundingRollingTariffThresholdAdjuster(var step: Double) : RollingTariffThresholdModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingTariffThreshold, value: Double): Double = roundTo(value, step)
}

data class FlaggedRollingTariffThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingTariffThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingTariffThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRollingTariffThresholdAdjuster(var cap: Double) : RollingTariffThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingTariffThreshold, value: Double): Double = minOf(value, cap)
}

data class RollingTariffThresholdGraceDaysOffsetAdjuster(var weight: Double) : RollingTariffThresholdModifier {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: RollingTariffThreshold, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

/** Runs the rolling tariff threshold steps in order and keeps a trace of each. */
data class RollingTariffThresholdEngine(
    var steps: MutableList<RollingTariffThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingTariffThreshold): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingTariffThresholdModifier = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingTariffThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingTariffThresholdEngine(flags: Registry): RollingTariffThresholdEngine {
    return RollingTariffThresholdEngine(
        steps = mutableListOf<RollingTariffThresholdModifier>(
            RoundingRollingTariffThresholdAdjuster(0.5),
            FlaggedRollingTariffThresholdAdjuster(flags, "rolling-tariff-threshold-batched", -6.5),
            CapRollingTariffThresholdAdjuster(8082.0),
            RollingTariffThresholdGraceDaysOffsetAdjuster(0.95),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 55252.6,
    )
}
