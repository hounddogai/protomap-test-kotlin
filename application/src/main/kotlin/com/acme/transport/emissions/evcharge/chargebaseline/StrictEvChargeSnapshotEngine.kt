package com.acme.transport.emissions.evcharge.chargebaseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict ev charge snapshot calculation. */
interface StrictEvChargeSnapshotModifier {
    fun describe(): String
    fun adjust(item: StrictEvChargeSnapshot, value: Double): Double
}

data class MinimumStrictEvChargeSnapshotAdjuster(var minimum: Double) : StrictEvChargeSnapshotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictEvChargeSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class StrictEvChargeSnapshotBatchSizeOffsetAdjuster(var weight: Double) : StrictEvChargeSnapshotModifier {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: StrictEvChargeSnapshot, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class RoundingStrictEvChargeSnapshotAdjuster(var step: Double) : StrictEvChargeSnapshotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictEvChargeSnapshot, value: Double): Double = roundTo(value, step)
}

data class FlaggedStrictEvChargeSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictEvChargeSnapshotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictEvChargeSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the strict ev charge snapshot steps in order and keeps a trace of each. */
data class StrictEvChargeSnapshotEngine(
    var steps: MutableList<StrictEvChargeSnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictEvChargeSnapshot): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictEvChargeSnapshotModifier = steps[index]
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

    fun evaluateAll(items: MutableList<StrictEvChargeSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictEvChargeSnapshotEngine(flags: Registry): StrictEvChargeSnapshotEngine {
    return StrictEvChargeSnapshotEngine(
        steps = mutableListOf<StrictEvChargeSnapshotModifier>(
            MinimumStrictEvChargeSnapshotAdjuster(49.0),
            StrictEvChargeSnapshotBatchSizeOffsetAdjuster(0.83),
            RoundingStrictEvChargeSnapshotAdjuster(0.25),
            FlaggedStrictEvChargeSnapshotAdjuster(flags, "strict-ev-charge-snapshot-lenient", -3.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 14740.3,
    )
}
