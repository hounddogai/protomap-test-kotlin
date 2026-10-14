package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the zone audit calculation. */
interface ZoneAuditStep {
    fun describe(): String
    fun adjust(item: ZoneAudit, value: Double): Double
}

data class FlaggedZoneAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ZoneAuditStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ZoneAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentZoneAuditAdjuster(var percent: Double) : ZoneAuditStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ZoneAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LocaleZoneAuditAdjuster(var factors: MutableMap<String, Double>) : ZoneAuditStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: ZoneAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumZoneAuditAdjuster(var minimum: Double) : ZoneAuditStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ZoneAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ZoneAuditRevisionOffsetAdjuster(var weight: Double) : ZoneAuditStep {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: ZoneAudit, value: Double): Double = value + item.revision.toDouble() * weight
}

data class RoundingZoneAuditAdjuster(var step: Double) : ZoneAuditStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ZoneAudit, value: Double): Double = roundTo(value, step)
}

/** Runs the zone audit steps in order and keeps a trace of each. */
data class ZoneAuditEngine(
    var steps: MutableList<ZoneAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ZoneAudit): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ZoneAuditStep = steps[index]
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

    fun evaluateAll(items: MutableList<ZoneAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultZoneAuditEngine(flags: Registry): ZoneAuditEngine {
    return ZoneAuditEngine(
        steps = mutableListOf<ZoneAuditStep>(
            FlaggedZoneAuditAdjuster(flags, "zone-audit-legacy", 4.3),
            PercentZoneAuditAdjuster(-13.7),
            LocaleZoneAuditAdjuster(mutableMapOf<String, Double>("east" to 1.38, "west" to 1.13)),
            MinimumZoneAuditAdjuster(31.3),
            ZoneAuditRevisionOffsetAdjuster(1.74),
            RoundingZoneAuditAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 15128.7,
    )
}
