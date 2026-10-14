package com.acme.transport.network.transittime.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the transit time variance calculation. */
interface TransitTimeVarianceModifier {
    fun describe(): String
    fun adjust(item: TransitTimeVariance, value: Double): Double
}

data class UnitTransitTimeVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : TransitTimeVarianceModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: TransitTimeVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredTransitTimeVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TransitTimeVarianceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TransitTimeVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingTransitTimeVarianceAdjuster(var step: Double) : TransitTimeVarianceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TransitTimeVariance, value: Double): Double = roundTo(value, step)
}

data class TransitTimeVarianceGraceDaysOffsetAdjuster(var weight: Double) : TransitTimeVarianceModifier {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: TransitTimeVariance, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class FlaggedTransitTimeVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TransitTimeVarianceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TransitTimeVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentTransitTimeVarianceAdjuster(var percent: Double) : TransitTimeVarianceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TransitTimeVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the transit time variance steps in order and keeps a trace of each. */
data class TransitTimeVarianceEngine(
    var steps: MutableList<TransitTimeVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TransitTimeVariance): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TransitTimeVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTransitTimeVarianceEngine(flags: Registry): TransitTimeVarianceEngine {
    return TransitTimeVarianceEngine(
        steps = mutableListOf<TransitTimeVarianceModifier>(
            UnitTransitTimeVarianceAdjuster(
                mutableMapOf<String, Double>("west" to 1.02, "retail" to 0.88, "central" to 0.8),
            ),
            TieredTransitTimeVarianceAdjuster(
                mutableListOf<Double>(1328.0, 5332.0, 5579.0, 6325.0),
                mutableListOf<Double>(0.84, 1.13, 1.1, 0.97),
            ),
            RoundingTransitTimeVarianceAdjuster(5.0),
            TransitTimeVarianceGraceDaysOffsetAdjuster(0.46),
            FlaggedTransitTimeVarianceAdjuster(flags, "transit-time-variance-seasonal", -6.4),
            PercentTransitTimeVarianceAdjuster(7.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 88092.5,
    )
}
