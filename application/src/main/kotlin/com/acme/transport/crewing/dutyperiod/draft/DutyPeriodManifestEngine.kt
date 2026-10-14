package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.kit.flags.Registry

/** One step of the duty period manifest calculation. */
interface DutyPeriodManifestModifier {
    fun describe(): String
    fun adjust(item: DutyPeriodManifest, value: Double): Double
}

data class PercentDutyPeriodManifestAdjuster(var percent: Double) : DutyPeriodManifestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DutyPeriodManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDutyPeriodManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DutyPeriodManifestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DutyPeriodManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DutyPeriodManifestQuantityOffsetAdjuster(var weight: Double) : DutyPeriodManifestModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: DutyPeriodManifest, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class SegmentDutyPeriodManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : DutyPeriodManifestModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: DutyPeriodManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumDutyPeriodManifestAdjuster(var minimum: Double) : DutyPeriodManifestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DutyPeriodManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredDutyPeriodManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DutyPeriodManifestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DutyPeriodManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the duty period manifest steps in order and keeps a trace of each. */
data class DutyPeriodManifestEngine(
    var steps: MutableList<DutyPeriodManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DutyPeriodManifest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DutyPeriodManifestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DutyPeriodManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDutyPeriodManifestEngine(flags: Registry): DutyPeriodManifestEngine {
    return DutyPeriodManifestEngine(
        steps = mutableListOf<DutyPeriodManifestModifier>(
            PercentDutyPeriodManifestAdjuster(-17.3),
            FlaggedDutyPeriodManifestAdjuster(flags, "duty-period-manifest-weighted", 6.4),
            DutyPeriodManifestQuantityOffsetAdjuster(1.89),
            SegmentDutyPeriodManifestAdjuster(
                mutableMapOf<String, Double>("online" to 0.85, "east" to 0.93, "north" to 0.7),
            ),
            MinimumDutyPeriodManifestAdjuster(7.6),
            TieredDutyPeriodManifestAdjuster(
                mutableListOf<Double>(8078.0, 8356.0),
                mutableListOf<Double>(1.15, 1.14),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 14979.5,
    )
}
