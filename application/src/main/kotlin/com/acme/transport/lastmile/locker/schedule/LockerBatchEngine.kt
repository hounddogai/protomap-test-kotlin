package com.acme.transport.lastmile.locker.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the locker batch calculation. */
interface LockerBatchAdjuster {
    fun describe(): String
    fun adjust(item: LockerBatch, value: Double): Double
}

data class LockerBatchMaximumOffsetAdjuster(var weight: Double) : LockerBatchAdjuster {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: LockerBatch, value: Double): Double = value + item.maximum * weight
}

data class PercentLockerBatchAdjuster(var percent: Double) : LockerBatchAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LockerBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLockerBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LockerBatchAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LockerBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLockerBatchAdjuster(var minimum: Double) : LockerBatchAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LockerBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the locker batch steps in order and keeps a trace of each. */
data class LockerBatchEngine(
    var steps: MutableList<LockerBatchAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LockerBatch): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LockerBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLockerBatchEngine(flags: Registry): LockerBatchEngine {
    return LockerBatchEngine(
        steps = mutableListOf<LockerBatchAdjuster>(
            LockerBatchMaximumOffsetAdjuster(0.99),
            PercentLockerBatchAdjuster(-10.9),
            FlaggedLockerBatchAdjuster(flags, "locker-batch-strict", -1.1),
            MinimumLockerBatchAdjuster(42.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 8198.0,
    )
}
