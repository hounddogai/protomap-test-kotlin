package com.acme.transport.customs.broker.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the broker snapshot calculation. */
interface BrokerSnapshotFactor {
    fun describe(): String
    fun adjust(item: BrokerSnapshot, value: Double): Double
}

data class RoundingBrokerSnapshotAdjuster(var step: Double) : BrokerSnapshotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerSnapshot, value: Double): Double = roundTo(value, step)
}

data class FlaggedBrokerSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BrokerSnapshotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BrokerSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentBrokerSnapshotAdjuster(var percent: Double) : BrokerSnapshotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class BrokerSnapshotRatioOffsetAdjuster(var weight: Double) : BrokerSnapshotFactor {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: BrokerSnapshot, value: Double): Double = value + item.ratio * weight
}

/** Runs the broker snapshot steps in order and keeps a trace of each. */
data class BrokerSnapshotEngine(
    var steps: MutableList<BrokerSnapshotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BrokerSnapshot): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BrokerSnapshotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BrokerSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBrokerSnapshotEngine(flags: Registry): BrokerSnapshotEngine {
    return BrokerSnapshotEngine(
        steps = mutableListOf<BrokerSnapshotFactor>(
            RoundingBrokerSnapshotAdjuster(0.25),
            FlaggedBrokerSnapshotAdjuster(flags, "broker-snapshot-regional", 7.8),
            PercentBrokerSnapshotAdjuster(23.2),
            BrokerSnapshotRatioOffsetAdjuster(0.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 50690.8,
    )
}
