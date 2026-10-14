package com.acme.transport.network.cutoff.cutofftarget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict cutoff target calculation. */
interface StrictCutoffTargetFactor {
    fun describe(): String
    fun adjust(item: StrictCutoffTarget, value: Double): Double
}

data class FlaggedStrictCutoffTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictCutoffTargetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictCutoffTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class StrictCutoffTargetBatchSizeOffsetAdjuster(var weight: Double) : StrictCutoffTargetFactor {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: StrictCutoffTarget, value: Double): Double = value + item.batchSize.toDouble() * weight
}

data class MinimumStrictCutoffTargetAdjuster(var minimum: Double) : StrictCutoffTargetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictCutoffTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingStrictCutoffTargetAdjuster(var step: Double) : StrictCutoffTargetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictCutoffTarget, value: Double): Double = roundTo(value, step)
}

data class NameStrictCutoffTargetAdjuster(var factors: MutableMap<String, Double>) : StrictCutoffTargetFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: StrictCutoffTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the strict cutoff target steps in order and keeps a trace of each. */
data class StrictCutoffTargetEngine(
    var steps: MutableList<StrictCutoffTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictCutoffTarget): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictCutoffTargetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictCutoffTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictCutoffTargetEngine(flags: Registry): StrictCutoffTargetEngine {
    return StrictCutoffTargetEngine(
        steps = mutableListOf<StrictCutoffTargetFactor>(
            FlaggedStrictCutoffTargetAdjuster(flags, "strict-cutoff-target-fallback", 1.6),
            StrictCutoffTargetBatchSizeOffsetAdjuster(1.04),
            MinimumStrictCutoffTargetAdjuster(36.3),
            RoundingStrictCutoffTargetAdjuster(5.0),
            NameStrictCutoffTargetAdjuster(mutableMapOf<String, Double>("retail" to 0.82, "west" to 0.89)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 8514.9,
    )
}
