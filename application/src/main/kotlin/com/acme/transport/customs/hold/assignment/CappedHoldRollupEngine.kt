package com.acme.transport.customs.hold.assignment

import com.acme.transport.kit.flags.Registry

/** One step of the capped hold rollup calculation. */
interface CappedHoldRollupModifier {
    fun describe(): String
    fun adjust(item: CappedHoldRollup, value: Double): Double
}

data class TitleCappedHoldRollupAdjuster(var factors: MutableMap<String, Double>) : CappedHoldRollupModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: CappedHoldRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCappedHoldRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedHoldRollupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedHoldRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CappedHoldRollupRevisionOffsetAdjuster(var weight: Double) : CappedHoldRollupModifier {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: CappedHoldRollup, value: Double): Double = value + item.revision.toDouble() * weight
}

/** Runs the capped hold rollup steps in order and keeps a trace of each. */
data class CappedHoldRollupEngine(
    var steps: MutableList<CappedHoldRollupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedHoldRollup): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedHoldRollupModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CappedHoldRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedHoldRollupEngine(flags: Registry): CappedHoldRollupEngine {
    return CappedHoldRollupEngine(
        steps = mutableListOf<CappedHoldRollupModifier>(
            TitleCappedHoldRollupAdjuster(
                mutableMapOf<String, Double>("online" to 1.21, "central" to 0.87, "retail" to 1.09),
            ),
            FlaggedCappedHoldRollupAdjuster(flags, "capped-hold-rollup-global", 6.0),
            CappedHoldRollupRevisionOffsetAdjuster(1.94),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 51457.4,
    )
}
