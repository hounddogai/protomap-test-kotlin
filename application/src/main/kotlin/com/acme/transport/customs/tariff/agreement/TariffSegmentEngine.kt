package com.acme.transport.customs.tariff.agreement

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tariff segment calculation. */
interface TariffSegmentStep {
    fun describe(): String
    fun adjust(item: TariffSegment, value: Double): Double
}

data class MinimumTariffSegmentAdjuster(var minimum: Double) : TariffSegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TariffSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingTariffSegmentAdjuster(var step: Double) : TariffSegmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TariffSegment, value: Double): Double = roundTo(value, step)
}

data class TieredTariffSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TariffSegmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TariffSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tariff segment steps in order and keeps a trace of each. */
data class TariffSegmentEngine(
    var steps: MutableList<TariffSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TariffSegment): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TariffSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTariffSegmentEngine(): TariffSegmentEngine {
    return TariffSegmentEngine(
        steps = mutableListOf<TariffSegmentStep>(
            MinimumTariffSegmentAdjuster(24.5),
            RoundingTariffSegmentAdjuster(5.0),
            TieredTariffSegmentAdjuster(
                mutableListOf<Double>(951.0, 5570.0, 6523.0, 8005.0),
                mutableListOf<Double>(1.08, 1.16, 1.06, 1.05),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 81037.9,
    )
}
