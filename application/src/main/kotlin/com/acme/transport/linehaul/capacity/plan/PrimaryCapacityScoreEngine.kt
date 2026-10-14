package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary capacity score calculation. */
interface PrimaryCapacityScoreAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryCapacityScore, value: Double): Double
}

data class PrimaryCapacityScoreSequenceOffsetAdjuster(var weight: Double) : PrimaryCapacityScoreAdjuster {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: PrimaryCapacityScore, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class RoundingPrimaryCapacityScoreAdjuster(var step: Double) : PrimaryCapacityScoreAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryCapacityScore, value: Double): Double = roundTo(value, step)
}

data class MinimumPrimaryCapacityScoreAdjuster(var minimum: Double) : PrimaryCapacityScoreAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryCapacityScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPrimaryCapacityScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryCapacityScoreAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryCapacityScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CountryPrimaryCapacityScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryCapacityScoreAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: PrimaryCapacityScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPrimaryCapacityScoreAdjuster(var percent: Double) : PrimaryCapacityScoreAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryCapacityScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the primary capacity score steps in order and keeps a trace of each. */
data class PrimaryCapacityScoreEngine(
    var steps: MutableList<PrimaryCapacityScoreAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryCapacityScore): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryCapacityScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryCapacityScoreEngine(flags: Registry): PrimaryCapacityScoreEngine {
    return PrimaryCapacityScoreEngine(
        steps = mutableListOf<PrimaryCapacityScoreAdjuster>(
            PrimaryCapacityScoreSequenceOffsetAdjuster(1.8),
            RoundingPrimaryCapacityScoreAdjuster(0.5),
            MinimumPrimaryCapacityScoreAdjuster(48.2),
            FlaggedPrimaryCapacityScoreAdjuster(flags, "primary-capacity-score-primary", -5.0),
            CountryPrimaryCapacityScoreAdjuster(mutableMapOf<String, Double>("east" to 0.95)),
            PercentPrimaryCapacityScoreAdjuster(-2.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 41270.7,
    )
}
