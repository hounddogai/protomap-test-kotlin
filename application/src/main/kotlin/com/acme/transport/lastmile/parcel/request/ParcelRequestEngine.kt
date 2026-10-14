package com.acme.transport.lastmile.parcel.request

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the parcel request calculation. */
interface ParcelRequestStep {
    fun describe(): String
    fun adjust(item: ParcelRequest, value: Double): Double
}

data class LabelParcelRequestAdjuster(var factors: MutableMap<String, Double>) : ParcelRequestStep {
    override fun describe(): String = "by label"

    override fun adjust(item: ParcelRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ParcelRequestRatioOffsetAdjuster(var weight: Double) : ParcelRequestStep {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: ParcelRequest, value: Double): Double = value + item.ratio * weight
}

data class MinimumParcelRequestAdjuster(var minimum: Double) : ParcelRequestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ParcelRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingParcelRequestAdjuster(var step: Double) : ParcelRequestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ParcelRequest, value: Double): Double = roundTo(value, step)
}

data class CapParcelRequestAdjuster(var cap: Double) : ParcelRequestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ParcelRequest, value: Double): Double = minOf(value, cap)
}

/** Runs the parcel request steps in order and keeps a trace of each. */
data class ParcelRequestEngine(
    var steps: MutableList<ParcelRequestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ParcelRequest): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ParcelRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultParcelRequestEngine(): ParcelRequestEngine {
    return ParcelRequestEngine(
        steps = mutableListOf<ParcelRequestStep>(
            LabelParcelRequestAdjuster(
                mutableMapOf<String, Double>("north" to 1.4, "east" to 1.28, "online" to 0.89),
            ),
            ParcelRequestRatioOffsetAdjuster(0.21),
            MinimumParcelRequestAdjuster(22.4),
            RoundingParcelRequestAdjuster(1.0),
            CapParcelRequestAdjuster(33067.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 3965.1,
    )
}
