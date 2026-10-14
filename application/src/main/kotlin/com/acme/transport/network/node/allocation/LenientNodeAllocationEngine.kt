package com.acme.transport.network.node.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient node allocation calculation. */
interface LenientNodeAllocationFactor {
    fun describe(): String
    fun adjust(item: LenientNodeAllocation, value: Double): Double
}

data class LabelLenientNodeAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientNodeAllocationFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: LenientNodeAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLenientNodeAllocationAdjuster(var percent: Double) : LenientNodeAllocationFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientNodeAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LenientNodeAllocationMinimumOffsetAdjuster(var weight: Double) : LenientNodeAllocationFactor {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: LenientNodeAllocation, value: Double): Double = value + item.minimum * weight
}

data class RoundingLenientNodeAllocationAdjuster(var step: Double) : LenientNodeAllocationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientNodeAllocation, value: Double): Double = roundTo(value, step)
}

data class FlaggedLenientNodeAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientNodeAllocationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientNodeAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lenient node allocation steps in order and keeps a trace of each. */
data class LenientNodeAllocationEngine(
    var steps: MutableList<LenientNodeAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientNodeAllocation): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientNodeAllocationFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LenientNodeAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientNodeAllocationEngine(flags: Registry): LenientNodeAllocationEngine {
    return LenientNodeAllocationEngine(
        steps = mutableListOf<LenientNodeAllocationFactor>(
            LabelLenientNodeAllocationAdjuster(mutableMapOf<String, Double>("east" to 0.79, "south" to 1.33)),
            PercentLenientNodeAllocationAdjuster(5.2),
            LenientNodeAllocationMinimumOffsetAdjuster(0.53),
            RoundingLenientNodeAllocationAdjuster(0.25),
            FlaggedLenientNodeAllocationAdjuster(flags, "lenient-node-allocation-rolling", 4.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 43093.6,
    )
}
