package com.acme.transport.freightpay.fuelsurcharge.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered fuel surcharge summary calculation. */
interface TieredFuelSurchargeSummaryAdjuster {
    fun describe(): String
    fun adjust(item: TieredFuelSurchargeSummary, value: Double): Double
}

data class RoundingTieredFuelSurchargeSummaryAdjuster(var step: Double) : TieredFuelSurchargeSummaryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredFuelSurchargeSummary, value: Double): Double = roundTo(value, step)
}

data class PercentTieredFuelSurchargeSummaryAdjuster(var percent: Double) : TieredFuelSurchargeSummaryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredFuelSurchargeSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedTieredFuelSurchargeSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredFuelSurchargeSummaryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredFuelSurchargeSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SourceTieredFuelSurchargeSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredFuelSurchargeSummaryAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: TieredFuelSurchargeSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTieredFuelSurchargeSummaryAdjuster(var minimum: Double) : TieredFuelSurchargeSummaryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredFuelSurchargeSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredTieredFuelSurchargeSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredFuelSurchargeSummaryAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredFuelSurchargeSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tiered fuel surcharge summary steps in order and keeps a trace of each. */
data class TieredFuelSurchargeSummaryEngine(
    var steps: MutableList<TieredFuelSurchargeSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredFuelSurchargeSummary): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredFuelSurchargeSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredFuelSurchargeSummaryEngine(flags: Registry): TieredFuelSurchargeSummaryEngine {
    return TieredFuelSurchargeSummaryEngine(
        steps = mutableListOf<TieredFuelSurchargeSummaryAdjuster>(
            RoundingTieredFuelSurchargeSummaryAdjuster(0.5),
            PercentTieredFuelSurchargeSummaryAdjuster(-22.6),
            FlaggedTieredFuelSurchargeSummaryAdjuster(flags, "tiered-fuel-surcharge-summary-global", 1.2),
            SourceTieredFuelSurchargeSummaryAdjuster(
                mutableMapOf<String, Double>("retail" to 0.79, "online" to 1.35, "north" to 0.93),
            ),
            MinimumTieredFuelSurchargeSummaryAdjuster(39.3),
            TieredTieredFuelSurchargeSummaryAdjuster(
                mutableListOf<Double>(4943.0, 5044.0),
                mutableListOf<Double>(0.88, 1.0),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 88822.4,
    )
}
