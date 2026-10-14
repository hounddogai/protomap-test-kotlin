package com.acme.transport.fleetops.defect.defectproposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered defect variance calculation. */
interface TieredDefectVarianceModifier {
    fun describe(): String
    fun adjust(item: TieredDefectVariance, value: Double): Double
}

data class RoundingTieredDefectVarianceAdjuster(var step: Double) : TieredDefectVarianceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredDefectVariance, value: Double): Double = roundTo(value, step)
}

data class MinimumTieredDefectVarianceAdjuster(var minimum: Double) : TieredDefectVarianceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredDefectVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTieredDefectVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredDefectVarianceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredDefectVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelTieredDefectVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredDefectVarianceModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: TieredDefectVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentTieredDefectVarianceAdjuster(var percent: Double) : TieredDefectVarianceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredDefectVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredTieredDefectVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredDefectVarianceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredDefectVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tiered defect variance steps in order and keeps a trace of each. */
data class TieredDefectVarianceEngine(
    var steps: MutableList<TieredDefectVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredDefectVariance): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredDefectVarianceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TieredDefectVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredDefectVarianceEngine(flags: Registry): TieredDefectVarianceEngine {
    return TieredDefectVarianceEngine(
        steps = mutableListOf<TieredDefectVarianceModifier>(
            RoundingTieredDefectVarianceAdjuster(0.25),
            MinimumTieredDefectVarianceAdjuster(12.1),
            FlaggedTieredDefectVarianceAdjuster(flags, "tiered-defect-variance-scheduled", -4.4),
            LabelTieredDefectVarianceAdjuster(mutableMapOf<String, Double>("retail" to 1.14, "online" to 0.76)),
            PercentTieredDefectVarianceAdjuster(-9.3),
            TieredTieredDefectVarianceAdjuster(
                mutableListOf<Double>(1395.0, 7761.0, 9953.0),
                mutableListOf<Double>(0.96, 0.84, 0.89),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 12772.6,
    )
}
