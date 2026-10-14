package com.acme.transport.network.zone.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped zone target calculation. */
interface CappedZoneTargetFactor {
    fun describe(): String
    fun adjust(item: CappedZoneTarget, value: Double): Double
}

data class FlaggedCappedZoneTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedZoneTargetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedZoneTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCappedZoneTargetAdjuster(var cap: Double) : CappedZoneTargetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedZoneTarget, value: Double): Double = minOf(value, cap)
}

data class PercentCappedZoneTargetAdjuster(var percent: Double) : CappedZoneTargetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedZoneTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCappedZoneTargetAdjuster(var step: Double) : CappedZoneTargetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedZoneTarget, value: Double): Double = roundTo(value, step)
}

data class TieredCappedZoneTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedZoneTargetFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedZoneTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped zone target steps in order and keeps a trace of each. */
data class CappedZoneTargetEngine(
    var steps: MutableList<CappedZoneTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedZoneTarget): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedZoneTargetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CappedZoneTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedZoneTargetEngine(flags: Registry): CappedZoneTargetEngine {
    return CappedZoneTargetEngine(
        steps = mutableListOf<CappedZoneTargetFactor>(
            FlaggedCappedZoneTargetAdjuster(flags, "capped-zone-target-tiered", -2.3),
            CapCappedZoneTargetAdjuster(39957.0),
            PercentCappedZoneTargetAdjuster(-18.0),
            RoundingCappedZoneTargetAdjuster(0.5),
            TieredCappedZoneTargetAdjuster(
                mutableListOf<Double>(453.0, 1099.0, 8756.0, 9040.0),
                mutableListOf<Double>(1.11, 1.13, 0.87, 0.94),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 29860.8,
    )
}
