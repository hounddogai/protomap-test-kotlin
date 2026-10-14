package com.acme.transport.lastmile.drop.quota

import com.acme.transport.kit.flags.Registry

/** One step of the primary drop profile calculation. */
interface PrimaryDropProfileModifier {
    fun describe(): String
    fun adjust(item: PrimaryDropProfile, value: Double): Double
}

data class CapPrimaryDropProfileAdjuster(var cap: Double) : PrimaryDropProfileModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryDropProfile, value: Double): Double = minOf(value, cap)
}

data class DepotPrimaryDropProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryDropProfileModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: PrimaryDropProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumPrimaryDropProfileAdjuster(var minimum: Double) : PrimaryDropProfileModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryDropProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredPrimaryDropProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryDropProfileModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryDropProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedPrimaryDropProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryDropProfileModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryDropProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PrimaryDropProfileCeilingOffsetAdjuster(var weight: Double) : PrimaryDropProfileModifier {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: PrimaryDropProfile, value: Double): Double = value + item.ceiling * weight
}

/** Runs the primary drop profile steps in order and keeps a trace of each. */
data class PrimaryDropProfileEngine(
    var steps: MutableList<PrimaryDropProfileModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryDropProfile): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryDropProfileModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryDropProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryDropProfileEngine(flags: Registry): PrimaryDropProfileEngine {
    return PrimaryDropProfileEngine(
        steps = mutableListOf<PrimaryDropProfileModifier>(
            CapPrimaryDropProfileAdjuster(36195.0),
            DepotPrimaryDropProfileAdjuster(mutableMapOf<String, Double>("west" to 0.9)),
            MinimumPrimaryDropProfileAdjuster(32.3),
            TieredPrimaryDropProfileAdjuster(
                mutableListOf<Double>(3466.0, 4399.0, 9803.0),
                mutableListOf<Double>(1.07, 1.12, 1.12),
            ),
            FlaggedPrimaryDropProfileAdjuster(flags, "primary-drop-profile-weighted", -0.8),
            PrimaryDropProfileCeilingOffsetAdjuster(0.86),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 40296.5,
    )
}
