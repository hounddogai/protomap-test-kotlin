package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the trailer yard record calculation. */
interface TrailerYardRecordModifier {
    fun describe(): String
    fun adjust(item: TrailerYardRecord, value: Double): Double
}

data class RoundingTrailerYardRecordAdjuster(var step: Double) : TrailerYardRecordModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerYardRecord, value: Double): Double = roundTo(value, step)
}

data class TieredTrailerYardRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerYardRecordModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerYardRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapTrailerYardRecordAdjuster(var cap: Double) : TrailerYardRecordModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerYardRecord, value: Double): Double = minOf(value, cap)
}

data class DepotTrailerYardRecordAdjuster(var factors: MutableMap<String, Double>) : TrailerYardRecordModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: TrailerYardRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TrailerYardRecordAttemptsOffsetAdjuster(var weight: Double) : TrailerYardRecordModifier {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: TrailerYardRecord, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class MinimumTrailerYardRecordAdjuster(var minimum: Double) : TrailerYardRecordModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerYardRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the trailer yard record steps in order and keeps a trace of each. */
data class TrailerYardRecordEngine(
    var steps: MutableList<TrailerYardRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TrailerYardRecord): Double {
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

    fun evaluateAll(items: MutableList<TrailerYardRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTrailerYardRecordEngine(): TrailerYardRecordEngine {
    return TrailerYardRecordEngine(
        steps = mutableListOf<TrailerYardRecordModifier>(
            RoundingTrailerYardRecordAdjuster(0.25),
            TieredTrailerYardRecordAdjuster(
                mutableListOf<Double>(2104.0, 4231.0, 7385.0),
                mutableListOf<Double>(1.09, 1.07, 1.15),
            ),
            CapTrailerYardRecordAdjuster(5064.0),
            DepotTrailerYardRecordAdjuster(mutableMapOf<String, Double>("south" to 0.78, "west" to 1.11)),
            TrailerYardRecordAttemptsOffsetAdjuster(0.56),
            MinimumTrailerYardRecordAdjuster(34.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 33816.4,
    )
}
