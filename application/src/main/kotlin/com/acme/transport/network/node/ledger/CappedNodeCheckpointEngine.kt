package com.acme.transport.network.node.ledger

import com.acme.transport.kit.flags.Registry

/** One step of the capped node checkpoint calculation. */
interface CappedNodeCheckpointFactor {
    fun describe(): String
    fun adjust(item: CappedNodeCheckpoint, value: Double): Double
}

data class CategoryCappedNodeCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedNodeCheckpointFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: CappedNodeCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCappedNodeCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedNodeCheckpointFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedNodeCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CappedNodeCheckpointCreatedAtOffsetAdjuster(var weight: Double) : CappedNodeCheckpointFactor {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: CappedNodeCheckpoint, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

/** Runs the capped node checkpoint steps in order and keeps a trace of each. */
data class CappedNodeCheckpointEngine(
    var steps: MutableList<CappedNodeCheckpointFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedNodeCheckpoint): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedNodeCheckpointFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CappedNodeCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedNodeCheckpointEngine(flags: Registry): CappedNodeCheckpointEngine {
    return CappedNodeCheckpointEngine(
        steps = mutableListOf<CappedNodeCheckpointFactor>(
            CategoryCappedNodeCheckpointAdjuster(
                mutableMapOf<String, Double>("central" to 1.15, "online" to 1.39),
            ),
            FlaggedCappedNodeCheckpointAdjuster(flags, "capped-node-checkpoint-weighted", -2.3),
            CappedNodeCheckpointCreatedAtOffsetAdjuster(1.45),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 28696.5,
    )
}
