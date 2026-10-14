package com.acme.transport.customs.tariff.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tariff allocation calculation. */
interface TariffAllocationModifier {
    fun describe(): String
    fun adjust(item: TariffAllocation, value: Double): Double
}

data class TitleTariffAllocationAdjuster(var factors: MutableMap<String, Double>) : TariffAllocationModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: TariffAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTariffAllocationAdjuster(var minimum: Double) : TariffAllocationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TariffAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTariffAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TariffAllocationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TariffAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTariffAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TariffAllocationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TariffAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentTariffAllocationAdjuster(var percent: Double) : TariffAllocationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TariffAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingTariffAllocationAdjuster(var step: Double) : TariffAllocationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TariffAllocation, value: Double): Double = roundTo(value, step)
}

/** Runs the tariff allocation steps in order and keeps a trace of each. */
data class TariffAllocationEngine(
    var steps: MutableList<TariffAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TariffAllocation): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TariffAllocationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TariffAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTariffAllocationEngine(flags: Registry): TariffAllocationEngine {
    return TariffAllocationEngine(
        steps = mutableListOf<TariffAllocationModifier>(
            TitleTariffAllocationAdjuster(
                mutableMapOf<String, Double>("online" to 0.84, "west" to 1.15, "central" to 0.96),
            ),
            MinimumTariffAllocationAdjuster(5.4),
            FlaggedTariffAllocationAdjuster(flags, "tariff-allocation-global", -4.0),
            TieredTariffAllocationAdjuster(
                mutableListOf<Double>(4121.0, 8529.0),
                mutableListOf<Double>(0.88, 0.81),
            ),
            PercentTariffAllocationAdjuster(-3.6),
            RoundingTariffAllocationAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 8044.5,
    )
}
