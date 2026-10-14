package com.acme.transport.customs.declaration.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the declaration batch calculation. */
interface DeclarationBatchAdjuster {
    fun describe(): String
    fun adjust(item: DeclarationBatch, value: Double): Double
}

data class FlaggedDeclarationBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DeclarationBatchAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DeclarationBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DeclarationBatchStopCountOffsetAdjuster(var weight: Double) : DeclarationBatchAdjuster {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: DeclarationBatch, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class RoundingDeclarationBatchAdjuster(var step: Double) : DeclarationBatchAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DeclarationBatch, value: Double): Double = roundTo(value, step)
}

data class PercentDeclarationBatchAdjuster(var percent: Double) : DeclarationBatchAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DeclarationBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumDeclarationBatchAdjuster(var minimum: Double) : DeclarationBatchAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DeclarationBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleDeclarationBatchAdjuster(var factors: MutableMap<String, Double>) : DeclarationBatchAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: DeclarationBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the declaration batch steps in order and keeps a trace of each. */
data class DeclarationBatchEngine(
    var steps: MutableList<DeclarationBatchAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DeclarationBatch): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DeclarationBatchAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DeclarationBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDeclarationBatchEngine(flags: Registry): DeclarationBatchEngine {
    return DeclarationBatchEngine(
        steps = mutableListOf<DeclarationBatchAdjuster>(
            FlaggedDeclarationBatchAdjuster(flags, "declaration-batch-global", -4.5),
            DeclarationBatchStopCountOffsetAdjuster(0.27),
            RoundingDeclarationBatchAdjuster(0.25),
            PercentDeclarationBatchAdjuster(-17.9),
            MinimumDeclarationBatchAdjuster(48.5),
            TitleDeclarationBatchAdjuster(mutableMapOf<String, Double>("central" to 1.23)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 86590.3,
    )
}
