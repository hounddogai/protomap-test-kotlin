package com.acme.transport.linehaul.relay.weightedrelayrule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending relay rollup calculation. */
interface PendingRelayRollupAdjuster {
    fun describe(): String
    fun adjust(item: PendingRelayRollup, value: Double): Double
}

data class PendingRelayRollupBatchSizeOffsetAdjuster(var weight: Double) : PendingRelayRollupAdjuster {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: PendingRelayRollup, value: Double): Double = value + item.batchSize.toDouble() * weight
}

data class FlaggedPendingRelayRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingRelayRollupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingRelayRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPendingRelayRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingRelayRollupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingRelayRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitlePendingRelayRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingRelayRollupAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: PendingRelayRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingPendingRelayRollupAdjuster(var step: Double) : PendingRelayRollupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingRelayRollup, value: Double): Double = roundTo(value, step)
}

/** Runs the pending relay rollup steps in order and keeps a trace of each. */
data class PendingRelayRollupEngine(
    var steps: MutableList<PendingRelayRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingRelayRollup): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingRelayRollupAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingRelayRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingRelayRollupEngine(flags: Registry): PendingRelayRollupEngine {
    return PendingRelayRollupEngine(
        steps = mutableListOf<PendingRelayRollupAdjuster>(
            PendingRelayRollupBatchSizeOffsetAdjuster(1.36),
            FlaggedPendingRelayRollupAdjuster(flags, "pending-relay-rollup-tiered", -3.2),
            TieredPendingRelayRollupAdjuster(
                mutableListOf<Double>(2283.0, 4454.0, 5888.0),
                mutableListOf<Double>(1.1, 1.08, 1.01),
            ),
            TitlePendingRelayRollupAdjuster(mutableMapOf<String, Double>("central" to 1.11, "east" to 1.03)),
            RoundingPendingRelayRollupAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 53080.1,
    )
}
