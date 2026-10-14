package com.acme.transport.emissions.emissionfactor.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted emission factor record calculation. */
interface WeightedEmissionFactorRecordModifier {
    fun describe(): String
    fun adjust(item: WeightedEmissionFactorRecord, value: Double): Double
}

data class PercentWeightedEmissionFactorRecordAdjuster(
    var percent: Double,
) : WeightedEmissionFactorRecordModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedEmissionFactorRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedWeightedEmissionFactorRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedEmissionFactorRecordModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedEmissionFactorRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumWeightedEmissionFactorRecordAdjuster(
    var minimum: Double,
) : WeightedEmissionFactorRecordModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedEmissionFactorRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameWeightedEmissionFactorRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedEmissionFactorRecordModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: WeightedEmissionFactorRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingWeightedEmissionFactorRecordAdjuster(
    var step: Double,
) : WeightedEmissionFactorRecordModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedEmissionFactorRecord, value: Double): Double = roundTo(value, step)
}

data class WeightedEmissionFactorRecordAmountOffsetAdjuster(
    var weight: Double,
) : WeightedEmissionFactorRecordModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: WeightedEmissionFactorRecord, value: Double): Double = value + item.amount * weight
}

/** Runs the weighted emission factor record steps in order and keeps a trace of each. */
data class WeightedEmissionFactorRecordEngine(
    var steps: MutableList<WeightedEmissionFactorRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedEmissionFactorRecord): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedEmissionFactorRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedEmissionFactorRecordEngine(flags: Registry): WeightedEmissionFactorRecordEngine {
    return WeightedEmissionFactorRecordEngine(
        steps = mutableListOf<WeightedEmissionFactorRecordModifier>(
            PercentWeightedEmissionFactorRecordAdjuster(-4.6),
            FlaggedWeightedEmissionFactorRecordAdjuster(flags, "weighted-emission-factor-record-regional", 0.4),
            MinimumWeightedEmissionFactorRecordAdjuster(15.9),
            NameWeightedEmissionFactorRecordAdjuster(mutableMapOf<String, Double>("south" to 1.19)),
            RoundingWeightedEmissionFactorRecordAdjuster(0.25),
            WeightedEmissionFactorRecordAmountOffsetAdjuster(1.92),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 21934.0,
    )
}
