package com.acme.transport.lastmile.locker.metric

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global locker record calculation. */
interface GlobalLockerRecordFactor {
    fun describe(): String
    fun adjust(item: GlobalLockerRecord, value: Double): Double
}

data class TieredGlobalLockerRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalLockerRecordFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalLockerRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SegmentGlobalLockerRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalLockerRecordFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: GlobalLockerRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingGlobalLockerRecordAdjuster(var step: Double) : GlobalLockerRecordFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalLockerRecord, value: Double): Double = roundTo(value, step)
}

/** Runs the global locker record steps in order and keeps a trace of each. */
data class GlobalLockerRecordEngine(
    var steps: MutableList<GlobalLockerRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalLockerRecord): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalLockerRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalLockerRecordEngine(): GlobalLockerRecordEngine {
    return GlobalLockerRecordEngine(
        steps = mutableListOf<GlobalLockerRecordFactor>(
            TieredGlobalLockerRecordAdjuster(
                mutableListOf<Double>(4882.0, 6242.0),
                mutableListOf<Double>(1.14, 0.87),
            ),
            SegmentGlobalLockerRecordAdjuster(mutableMapOf<String, Double>("north" to 1.13, "east" to 1.36)),
            RoundingGlobalLockerRecordAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 60005.6,
    )
}
