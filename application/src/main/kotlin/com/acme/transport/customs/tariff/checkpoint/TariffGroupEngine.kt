package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tariff group calculation. */
interface TariffGroupModifier {
    fun describe(): String
    fun adjust(item: TariffGroup, value: Double): Double
}

data class RoundingTariffGroupAdjuster(var step: Double) : TariffGroupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TariffGroup, value: Double): Double = roundTo(value, step)
}

data class MinimumTariffGroupAdjuster(var minimum: Double) : TariffGroupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TariffGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredTariffGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TariffGroupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TariffGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SegmentTariffGroupAdjuster(var factors: MutableMap<String, Double>) : TariffGroupModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: TariffGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tariff group steps in order and keeps a trace of each. */
data class TariffGroupEngine(
    var steps: MutableList<TariffGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TariffGroup): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TariffGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTariffGroupEngine(): TariffGroupEngine {
    return TariffGroupEngine(
        steps = mutableListOf<TariffGroupModifier>(
            RoundingTariffGroupAdjuster(5.0),
            MinimumTariffGroupAdjuster(6.9),
            TieredTariffGroupAdjuster(mutableListOf<Double>(1731.0, 8301.0), mutableListOf<Double>(0.98, 1.06)),
            SegmentTariffGroupAdjuster(mutableMapOf<String, Double>("north" to 0.82, "east" to 0.79)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 74742.6,
    )
}
