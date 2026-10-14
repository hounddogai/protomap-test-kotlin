package com.acme.transport.lastmile.parcel.score

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending parcel request calculation. */
interface PendingParcelRequestModifier {
    fun describe(): String
    fun adjust(item: PendingParcelRequest, value: Double): Double
}

data class CapPendingParcelRequestAdjuster(var cap: Double) : PendingParcelRequestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingParcelRequest, value: Double): Double = minOf(value, cap)
}

data class TieredPendingParcelRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingParcelRequestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingParcelRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentPendingParcelRequestAdjuster(var percent: Double) : PendingParcelRequestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingParcelRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumPendingParcelRequestAdjuster(var minimum: Double) : PendingParcelRequestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingParcelRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PendingParcelRequestAttemptsOffsetAdjuster(var weight: Double) : PendingParcelRequestModifier {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: PendingParcelRequest, value: Double): Double = value + item.attempts.toDouble() * weight
}

/** Runs the pending parcel request steps in order and keeps a trace of each. */
data class PendingParcelRequestEngine(
    var steps: MutableList<PendingParcelRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingParcelRequest): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingParcelRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingParcelRequestEngine(): PendingParcelRequestEngine {
    return PendingParcelRequestEngine(
        steps = mutableListOf<PendingParcelRequestModifier>(
            CapPendingParcelRequestAdjuster(36211.0),
            TieredPendingParcelRequestAdjuster(
                mutableListOf<Double>(718.0, 4453.0, 8319.0),
                mutableListOf<Double>(0.82, 0.82, 0.91),
            ),
            PercentPendingParcelRequestAdjuster(-14.2),
            MinimumPendingParcelRequestAdjuster(19.5),
            PendingParcelRequestAttemptsOffsetAdjuster(0.84),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 22095.5,
    )
}
