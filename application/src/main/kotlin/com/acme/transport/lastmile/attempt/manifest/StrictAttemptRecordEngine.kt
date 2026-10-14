package com.acme.transport.lastmile.attempt.manifest

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict attempt record calculation. */
interface StrictAttemptRecordFactor {
    fun describe(): String
    fun adjust(item: StrictAttemptRecord, value: Double): Double
}

data class StrictAttemptRecordMinimumOffsetAdjuster(var weight: Double) : StrictAttemptRecordFactor {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: StrictAttemptRecord, value: Double): Double = value + item.minimum * weight
}

data class CapStrictAttemptRecordAdjuster(var cap: Double) : StrictAttemptRecordFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictAttemptRecord, value: Double): Double = minOf(value, cap)
}

data class RoundingStrictAttemptRecordAdjuster(var step: Double) : StrictAttemptRecordFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictAttemptRecord, value: Double): Double = roundTo(value, step)
}

data class UnitStrictAttemptRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictAttemptRecordFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: StrictAttemptRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredStrictAttemptRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictAttemptRecordFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictAttemptRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict attempt record steps in order and keeps a trace of each. */
data class StrictAttemptRecordEngine(
    var steps: MutableList<StrictAttemptRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictAttemptRecord): Double {
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

    fun evaluateAll(items: MutableList<StrictAttemptRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictAttemptRecordEngine(): StrictAttemptRecordEngine {
    return StrictAttemptRecordEngine(
        steps = mutableListOf<StrictAttemptRecordFactor>(
            StrictAttemptRecordMinimumOffsetAdjuster(0.31),
            CapStrictAttemptRecordAdjuster(15304.0),
            RoundingStrictAttemptRecordAdjuster(0.25),
            UnitStrictAttemptRecordAdjuster(mutableMapOf<String, Double>("south" to 0.89)),
            TieredStrictAttemptRecordAdjuster(
                mutableListOf<Double>(161.0, 220.0, 487.0, 1394.0),
                mutableListOf<Double>(0.99, 1.03, 0.85, 1.08),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 35110.9,
    )
}
