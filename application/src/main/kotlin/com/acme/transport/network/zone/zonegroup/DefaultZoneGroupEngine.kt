package com.acme.transport.network.zone.zonegroup

import com.acme.transport.kit.flags.Registry

/** One step of the default zone group calculation. */
interface DefaultZoneGroupStep {
    fun describe(): String
    fun adjust(item: DefaultZoneGroup, value: Double): Double
}

data class DepotDefaultZoneGroupAdjuster(var factors: MutableMap<String, Double>) : DefaultZoneGroupStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: DefaultZoneGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentDefaultZoneGroupAdjuster(var percent: Double) : DefaultZoneGroupStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultZoneGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDefaultZoneGroupAdjuster(var cap: Double) : DefaultZoneGroupStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultZoneGroup, value: Double): Double = minOf(value, cap)
}

data class MinimumDefaultZoneGroupAdjuster(var minimum: Double) : DefaultZoneGroupStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultZoneGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedDefaultZoneGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultZoneGroupStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultZoneGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DefaultZoneGroupRetriesOffsetAdjuster(var weight: Double) : DefaultZoneGroupStep {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: DefaultZoneGroup, value: Double): Double = value + item.retries.toDouble() * weight
}

/** Runs the default zone group steps in order and keeps a trace of each. */
data class DefaultZoneGroupEngine(
    var steps: MutableList<DefaultZoneGroupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultZoneGroup): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultZoneGroupStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultZoneGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultZoneGroupEngine(flags: Registry): DefaultZoneGroupEngine {
    return DefaultZoneGroupEngine(
        steps = mutableListOf<DefaultZoneGroupStep>(
            DepotDefaultZoneGroupAdjuster(
                mutableMapOf<String, Double>("retail" to 1.17, "north" to 1.23, "central" to 1.3),
            ),
            PercentDefaultZoneGroupAdjuster(11.6),
            CapDefaultZoneGroupAdjuster(7090.0),
            MinimumDefaultZoneGroupAdjuster(3.2),
            FlaggedDefaultZoneGroupAdjuster(flags, "default-zone-group-default", 6.3),
            DefaultZoneGroupRetriesOffsetAdjuster(1.52),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 77169.4,
    )
}
