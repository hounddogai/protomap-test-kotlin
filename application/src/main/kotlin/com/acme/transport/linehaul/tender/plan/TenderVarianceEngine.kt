package com.acme.transport.linehaul.tender.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tender variance calculation. */
interface TenderVarianceFactor {
    fun describe(): String
    fun adjust(item: TenderVariance, value: Double): Double
}

data class FlaggedTenderVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TenderVarianceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TenderVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumTenderVarianceAdjuster(var minimum: Double) : TenderVarianceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TenderVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentTenderVarianceAdjuster(var percent: Double) : TenderVarianceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TenderVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredTenderVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TenderVarianceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TenderVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tender variance steps in order and keeps a trace of each. */
data class TenderVarianceEngine(
    var steps: MutableList<TenderVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TenderVariance): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TenderVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTenderVarianceEngine(flags: Registry): TenderVarianceEngine {
    return TenderVarianceEngine(
        steps = mutableListOf<TenderVarianceFactor>(
            FlaggedTenderVarianceAdjuster(flags, "tender-variance-weighted", -1.5),
            MinimumTenderVarianceAdjuster(22.5),
            PercentTenderVarianceAdjuster(-25.0),
            TieredTenderVarianceAdjuster(
                mutableListOf<Double>(2751.0, 4133.0, 7865.0, 9903.0),
                mutableListOf<Double>(0.94, 0.95, 0.93, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 25717.1,
    )
}
