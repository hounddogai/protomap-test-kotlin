package com.acme.transport.terminal.dwell.ledger

import com.acme.transport.kit.flags.Registry

/** One step of the capped dwell batch calculation. */
interface CappedDwellBatchModifier {
    fun describe(): String
    fun adjust(item: CappedDwellBatch, value: Double): Double
}

data class FlaggedCappedDwellBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedDwellBatchModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedDwellBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameCappedDwellBatchAdjuster(var factors: MutableMap<String, Double>) : CappedDwellBatchModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: CappedDwellBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumCappedDwellBatchAdjuster(var minimum: Double) : CappedDwellBatchModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedDwellBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the capped dwell batch steps in order and keeps a trace of each. */
data class CappedDwellBatchEngine(
    var steps: MutableList<CappedDwellBatchModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedDwellBatch): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedDwellBatchModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CappedDwellBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedDwellBatchEngine(flags: Registry): CappedDwellBatchEngine {
    return CappedDwellBatchEngine(
        steps = mutableListOf<CappedDwellBatchModifier>(
            FlaggedCappedDwellBatchAdjuster(flags, "capped-dwell-batch-capped", -9.5),
            NameCappedDwellBatchAdjuster(mutableMapOf<String, Double>("north" to 1.32, "retail" to 1.24)),
            MinimumCappedDwellBatchAdjuster(45.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 63070.0,
    )
}
