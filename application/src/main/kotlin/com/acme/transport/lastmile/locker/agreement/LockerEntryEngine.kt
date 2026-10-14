package com.acme.transport.lastmile.locker.agreement

import com.acme.transport.kit.flags.Registry

/** One step of the locker entry calculation. */
interface LockerEntryStep {
    fun describe(): String
    fun adjust(item: LockerEntry, value: Double): Double
}

data class FlaggedLockerEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LockerEntryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LockerEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LockerEntryRatioOffsetAdjuster(var weight: Double) : LockerEntryStep {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: LockerEntry, value: Double): Double = value + item.ratio * weight
}

data class TieredLockerEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LockerEntryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LockerEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLockerEntryAdjuster(var percent: Double) : LockerEntryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LockerEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLockerEntryAdjuster(var cap: Double) : LockerEntryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LockerEntry, value: Double): Double = minOf(value, cap)
}

/** Runs the locker entry steps in order and keeps a trace of each. */
data class LockerEntryEngine(
    var steps: MutableList<LockerEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LockerEntry): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LockerEntryStep = steps[index]
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

    fun evaluateAll(items: MutableList<LockerEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLockerEntryEngine(flags: Registry): LockerEntryEngine {
    return LockerEntryEngine(
        steps = mutableListOf<LockerEntryStep>(
            FlaggedLockerEntryAdjuster(flags, "locker-entry-adaptive", 1.2),
            LockerEntryRatioOffsetAdjuster(0.75),
            TieredLockerEntryAdjuster(
                mutableListOf<Double>(888.0, 2571.0, 5728.0, 5883.0),
                mutableListOf<Double>(1.11, 1.03, 0.83, 0.81),
            ),
            PercentLockerEntryAdjuster(16.3),
            CapLockerEntryAdjuster(14694.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 50191.5,
    )
}
