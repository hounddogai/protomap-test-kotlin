package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the dwell quota calculation. */
interface DwellQuotaModifier {
    fun describe(): String
    fun adjust(item: DwellQuota, value: Double): Double
}

data class CapDwellQuotaAdjuster(var cap: Double) : DwellQuotaModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DwellQuota, value: Double): Double = minOf(value, cap)
}

data class RoundingDwellQuotaAdjuster(var step: Double) : DwellQuotaModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DwellQuota, value: Double): Double = roundTo(value, step)
}

data class FlaggedDwellQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DwellQuotaModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DwellQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumDwellQuotaAdjuster(var minimum: Double) : DwellQuotaModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DwellQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DwellQuotaWindowDaysOffsetAdjuster(var weight: Double) : DwellQuotaModifier {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: DwellQuota, value: Double): Double = value + item.windowDays.toDouble() * weight
}

data class TieredDwellQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DwellQuotaModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DwellQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the dwell quota steps in order and keeps a trace of each. */
data class DwellQuotaEngine(
    var steps: MutableList<DwellQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DwellQuota): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DwellQuotaModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DwellQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDwellQuotaEngine(flags: Registry): DwellQuotaEngine {
    return DwellQuotaEngine(
        steps = mutableListOf<DwellQuotaModifier>(
            CapDwellQuotaAdjuster(42473.0),
            RoundingDwellQuotaAdjuster(5.0),
            FlaggedDwellQuotaAdjuster(flags, "dwell-quota-manual", 8.6),
            MinimumDwellQuotaAdjuster(28.3),
            DwellQuotaWindowDaysOffsetAdjuster(1.42),
            TieredDwellQuotaAdjuster(
                mutableListOf<Double>(550.0, 4528.0, 5011.0),
                mutableListOf<Double>(1.06, 0.94, 0.9),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 29415.9,
    )
}
