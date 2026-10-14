package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled emission factor rollup calculation. */
interface ScheduledEmissionFactorRollupStep {
    fun describe(): String
    fun adjust(item: ScheduledEmissionFactorRollup, value: Double): Double
}

data class TieredScheduledEmissionFactorRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledEmissionFactorRollupStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledEmissionFactorRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentScheduledEmissionFactorRollupAdjuster(
    var percent: Double,
) : ScheduledEmissionFactorRollupStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledEmissionFactorRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleScheduledEmissionFactorRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledEmissionFactorRollupStep {
    override fun describe(): String = "by title"

    override fun adjust(item: ScheduledEmissionFactorRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingScheduledEmissionFactorRollupAdjuster(var step: Double) : ScheduledEmissionFactorRollupStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledEmissionFactorRollup, value: Double): Double = roundTo(value, step)
}

data class FlaggedScheduledEmissionFactorRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledEmissionFactorRollupStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledEmissionFactorRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ScheduledEmissionFactorRollupCreatedAtOffsetAdjuster(
    var weight: Double,
) : ScheduledEmissionFactorRollupStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: ScheduledEmissionFactorRollup, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

/** Runs the scheduled emission factor rollup steps in order and keeps a trace of each. */
data class ScheduledEmissionFactorRollupEngine(
    var steps: MutableList<ScheduledEmissionFactorRollupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledEmissionFactorRollup): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledEmissionFactorRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledEmissionFactorRollupEngine(flags: Registry): ScheduledEmissionFactorRollupEngine {
    return ScheduledEmissionFactorRollupEngine(
        steps = mutableListOf<ScheduledEmissionFactorRollupStep>(
            TieredScheduledEmissionFactorRollupAdjuster(
                mutableListOf<Double>(358.0, 3914.0, 7663.0),
                mutableListOf<Double>(1.09, 0.88, 1.13),
            ),
            PercentScheduledEmissionFactorRollupAdjuster(-12.7),
            TitleScheduledEmissionFactorRollupAdjuster(mutableMapOf<String, Double>("online" to 1.07)),
            RoundingScheduledEmissionFactorRollupAdjuster(1.0),
            FlaggedScheduledEmissionFactorRollupAdjuster(flags, "scheduled-emission-factor-rollup-capped", 3.3),
            ScheduledEmissionFactorRollupCreatedAtOffsetAdjuster(0.79),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 87020.3,
    )
}
