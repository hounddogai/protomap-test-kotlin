package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the redelivery snapshot calculation. */
interface RedeliverySnapshotModifier {
    fun describe(): String
    fun adjust(item: RedeliverySnapshot, value: Double): Double
}

data class FlaggedRedeliverySnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RedeliverySnapshotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RedeliverySnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRedeliverySnapshotAdjuster(var step: Double) : RedeliverySnapshotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RedeliverySnapshot, value: Double): Double = roundTo(value, step)
}

data class PercentRedeliverySnapshotAdjuster(var percent: Double) : RedeliverySnapshotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RedeliverySnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the redelivery snapshot steps in order and keeps a trace of each. */
data class RedeliverySnapshotEngine(
    var steps: MutableList<RedeliverySnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RedeliverySnapshot): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RedeliverySnapshotModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RedeliverySnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRedeliverySnapshotEngine(flags: Registry): RedeliverySnapshotEngine {
    return RedeliverySnapshotEngine(
        steps = mutableListOf<RedeliverySnapshotModifier>(
            FlaggedRedeliverySnapshotAdjuster(flags, "redelivery-snapshot-seasonal", 1.9),
            RoundingRedeliverySnapshotAdjuster(0.5),
            PercentRedeliverySnapshotAdjuster(10.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 53414.1,
    )
}
