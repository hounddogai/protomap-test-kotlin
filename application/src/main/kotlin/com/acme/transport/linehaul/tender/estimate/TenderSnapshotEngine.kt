package com.acme.transport.linehaul.tender.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tender snapshot calculation. */
interface TenderSnapshotAdjuster {
    fun describe(): String
    fun adjust(item: TenderSnapshot, value: Double): Double
}

data class MinimumTenderSnapshotAdjuster(var minimum: Double) : TenderSnapshotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TenderSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingTenderSnapshotAdjuster(var step: Double) : TenderSnapshotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TenderSnapshot, value: Double): Double = roundTo(value, step)
}

data class TenderSnapshotRateOffsetAdjuster(var weight: Double) : TenderSnapshotAdjuster {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: TenderSnapshot, value: Double): Double = value + item.rate * weight
}

data class UnitTenderSnapshotAdjuster(var factors: MutableMap<String, Double>) : TenderSnapshotAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: TenderSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedTenderSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TenderSnapshotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TenderSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentTenderSnapshotAdjuster(var percent: Double) : TenderSnapshotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TenderSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tender snapshot steps in order and keeps a trace of each. */
data class TenderSnapshotEngine(
    var steps: MutableList<TenderSnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TenderSnapshot): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TenderSnapshotAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TenderSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTenderSnapshotEngine(flags: Registry): TenderSnapshotEngine {
    return TenderSnapshotEngine(
        steps = mutableListOf<TenderSnapshotAdjuster>(
            MinimumTenderSnapshotAdjuster(44.7),
            RoundingTenderSnapshotAdjuster(1.0),
            TenderSnapshotRateOffsetAdjuster(0.65),
            UnitTenderSnapshotAdjuster(mutableMapOf<String, Double>("online" to 0.7)),
            FlaggedTenderSnapshotAdjuster(flags, "tender-snapshot-fallback", 5.9),
            PercentTenderSnapshotAdjuster(18.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 38136.4,
    )
}
