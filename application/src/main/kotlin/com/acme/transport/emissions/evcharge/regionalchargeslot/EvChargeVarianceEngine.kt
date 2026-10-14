package com.acme.transport.emissions.evcharge.regionalchargeslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the ev charge variance calculation. */
interface EvChargeVarianceModifier {
    fun describe(): String
    fun adjust(item: EvChargeVariance, value: Double): Double
}

data class PercentEvChargeVarianceAdjuster(var percent: Double) : EvChargeVarianceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EvChargeVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumEvChargeVarianceAdjuster(var minimum: Double) : EvChargeVarianceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class EvChargeVarianceQuantityOffsetAdjuster(var weight: Double) : EvChargeVarianceModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: EvChargeVariance, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class RoundingEvChargeVarianceAdjuster(var step: Double) : EvChargeVarianceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EvChargeVariance, value: Double): Double = roundTo(value, step)
}

data class ChannelEvChargeVarianceAdjuster(var factors: MutableMap<String, Double>) : EvChargeVarianceModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: EvChargeVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedEvChargeVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargeVarianceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargeVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the ev charge variance steps in order and keeps a trace of each. */
data class EvChargeVarianceEngine(
    var steps: MutableList<EvChargeVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: EvChargeVariance): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EvChargeVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultEvChargeVarianceEngine(flags: Registry): EvChargeVarianceEngine {
    return EvChargeVarianceEngine(
        steps = mutableListOf<EvChargeVarianceModifier>(
            PercentEvChargeVarianceAdjuster(-2.5),
            MinimumEvChargeVarianceAdjuster(39.5),
            EvChargeVarianceQuantityOffsetAdjuster(1.9),
            RoundingEvChargeVarianceAdjuster(0.25),
            ChannelEvChargeVarianceAdjuster(
                mutableMapOf<String, Double>("online" to 1.11, "central" to 0.75, "south" to 1.35),
            ),
            FlaggedEvChargeVarianceAdjuster(flags, "ev-charge-variance-scheduled", 5.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 79187.7,
    )
}
