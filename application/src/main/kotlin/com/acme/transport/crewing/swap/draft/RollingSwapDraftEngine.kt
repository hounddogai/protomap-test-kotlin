package com.acme.transport.crewing.swap.draft

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling swap draft calculation. */
interface RollingSwapDraftFactor {
    fun describe(): String
    fun adjust(item: RollingSwapDraft, value: Double): Double
}

data class RoundingRollingSwapDraftAdjuster(var step: Double) : RollingSwapDraftFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingSwapDraft, value: Double): Double = roundTo(value, step)
}

data class RollingSwapDraftRetriesOffsetAdjuster(var weight: Double) : RollingSwapDraftFactor {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: RollingSwapDraft, value: Double): Double = value + item.retries.toDouble() * weight
}

data class CategoryRollingSwapDraftAdjuster(var factors: MutableMap<String, Double>) : RollingSwapDraftFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: RollingSwapDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRollingSwapDraftAdjuster(var minimum: Double) : RollingSwapDraftFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingSwapDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling swap draft steps in order and keeps a trace of each. */
data class RollingSwapDraftEngine(
    var steps: MutableList<RollingSwapDraftFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingSwapDraft): Double {
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

    fun evaluateAll(items: MutableList<RollingSwapDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingSwapDraftEngine(): RollingSwapDraftEngine {
    return RollingSwapDraftEngine(
        steps = mutableListOf<RollingSwapDraftFactor>(
            RoundingRollingSwapDraftAdjuster(5.0),
            RollingSwapDraftRetriesOffsetAdjuster(0.95),
            CategoryRollingSwapDraftAdjuster(
                mutableMapOf<String, Double>("east" to 1.31, "south" to 0.86, "west" to 0.9),
            ),
            MinimumRollingSwapDraftAdjuster(25.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 59945.7,
    )
}
