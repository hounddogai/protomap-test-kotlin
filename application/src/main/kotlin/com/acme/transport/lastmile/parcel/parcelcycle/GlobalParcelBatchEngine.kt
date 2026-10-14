package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global parcel batch calculation. */
interface GlobalParcelBatchStep {
    fun describe(): String
    fun adjust(item: GlobalParcelBatch, value: Double): Double
}

data class MinimumGlobalParcelBatchAdjuster(var minimum: Double) : GlobalParcelBatchStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalParcelBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedGlobalParcelBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalParcelBatchStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalParcelBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapGlobalParcelBatchAdjuster(var cap: Double) : GlobalParcelBatchStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalParcelBatch, value: Double): Double = minOf(value, cap)
}

data class GlobalParcelBatchLoadKgOffsetAdjuster(var weight: Double) : GlobalParcelBatchStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: GlobalParcelBatch, value: Double): Double = value + item.loadKg * weight
}

data class PercentGlobalParcelBatchAdjuster(var percent: Double) : GlobalParcelBatchStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalParcelBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingGlobalParcelBatchAdjuster(var step: Double) : GlobalParcelBatchStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalParcelBatch, value: Double): Double = roundTo(value, step)
}

/** Runs the global parcel batch steps in order and keeps a trace of each. */
data class GlobalParcelBatchEngine(
    var steps: MutableList<GlobalParcelBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalParcelBatch): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalParcelBatchStep = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalParcelBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalParcelBatchEngine(flags: Registry): GlobalParcelBatchEngine {
    return GlobalParcelBatchEngine(
        steps = mutableListOf<GlobalParcelBatchStep>(
            MinimumGlobalParcelBatchAdjuster(15.1),
            FlaggedGlobalParcelBatchAdjuster(flags, "global-parcel-batch-seasonal", -5.5),
            CapGlobalParcelBatchAdjuster(8672.0),
            GlobalParcelBatchLoadKgOffsetAdjuster(0.53),
            PercentGlobalParcelBatchAdjuster(16.2),
            RoundingGlobalParcelBatchAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 3793.3,
    )
}
