package com.acme.transport.emissions.evcharge.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary ev charge snapshot calculation. */
interface PrimaryEvChargeSnapshotModifier {
    fun describe(): String
    fun adjust(item: PrimaryEvChargeSnapshot, value: Double): Double
}

data class RoundingPrimaryEvChargeSnapshotAdjuster(var step: Double) : PrimaryEvChargeSnapshotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryEvChargeSnapshot, value: Double): Double = roundTo(value, step)
}

data class PrimaryEvChargeSnapshotDurationMinutesOffsetAdjuster(
    var weight: Double,
) : PrimaryEvChargeSnapshotModifier {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: PrimaryEvChargeSnapshot, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class TieredPrimaryEvChargeSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryEvChargeSnapshotModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryEvChargeSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedPrimaryEvChargeSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryEvChargeSnapshotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryEvChargeSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPrimaryEvChargeSnapshotAdjuster(var cap: Double) : PrimaryEvChargeSnapshotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryEvChargeSnapshot, value: Double): Double = minOf(value, cap)
}

/** Runs the primary ev charge snapshot steps in order and keeps a trace of each. */
data class PrimaryEvChargeSnapshotEngine(
    var steps: MutableList<PrimaryEvChargeSnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryEvChargeSnapshot): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryEvChargeSnapshotModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryEvChargeSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryEvChargeSnapshotEngine(flags: Registry): PrimaryEvChargeSnapshotEngine {
    return PrimaryEvChargeSnapshotEngine(
        steps = mutableListOf<PrimaryEvChargeSnapshotModifier>(
            RoundingPrimaryEvChargeSnapshotAdjuster(5.0),
            PrimaryEvChargeSnapshotDurationMinutesOffsetAdjuster(0.39),
            TieredPrimaryEvChargeSnapshotAdjuster(
                mutableListOf<Double>(4038.0, 6784.0, 7720.0, 8095.0),
                mutableListOf<Double>(0.94, 1.13, 1.13, 1.17),
            ),
            FlaggedPrimaryEvChargeSnapshotAdjuster(flags, "primary-ev-charge-snapshot-primary", -6.2),
            CapPrimaryEvChargeSnapshotAdjuster(29514.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 31082.5,
    )
}
