package com.acme.transport.linehaul.tender.recorditem

import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient tender checkpoint calculation. */
interface LenientTenderCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: LenientTenderCheckpoint, value: Double): Double
}

data class LenientTenderCheckpointRetriesOffsetAdjuster(var weight: Double) : LenientTenderCheckpointAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: LenientTenderCheckpoint, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class TieredLenientTenderCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientTenderCheckpointAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientTenderCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLenientTenderCheckpointAdjuster(var step: Double) : LenientTenderCheckpointAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientTenderCheckpoint, value: Double): Double = roundTo(value, step)
}

/** Runs the lenient tender checkpoint steps in order and keeps a trace of each. */
data class LenientTenderCheckpointEngine(
    var steps: MutableList<LenientTenderCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientTenderCheckpoint): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientTenderCheckpointAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LenientTenderCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientTenderCheckpointEngine(): LenientTenderCheckpointEngine {
    return LenientTenderCheckpointEngine(
        steps = mutableListOf<LenientTenderCheckpointAdjuster>(
            LenientTenderCheckpointRetriesOffsetAdjuster(1.38),
            TieredLenientTenderCheckpointAdjuster(
                mutableListOf<Double>(1000.0, 4560.0, 7047.0),
                mutableListOf<Double>(0.99, 0.91, 0.88),
            ),
            RoundingLenientTenderCheckpointAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 63380.0,
    )
}
