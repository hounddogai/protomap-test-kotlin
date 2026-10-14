package com.acme.transport.safety.cameraevent.globaleventaudit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy camera event audit calculation. */
interface LegacyCameraEventAuditModifier {
    fun describe(): String
    fun adjust(item: LegacyCameraEventAudit, value: Double): Double
}

data class RoundingLegacyCameraEventAuditAdjuster(var step: Double) : LegacyCameraEventAuditModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyCameraEventAudit, value: Double): Double = roundTo(value, step)
}

data class FlaggedLegacyCameraEventAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyCameraEventAuditModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyCameraEventAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLegacyCameraEventAuditAdjuster(var percent: Double) : LegacyCameraEventAuditModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyCameraEventAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLegacyCameraEventAuditAdjuster(var minimum: Double) : LegacyCameraEventAuditModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyCameraEventAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleLegacyCameraEventAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyCameraEventAuditModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: LegacyCameraEventAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the legacy camera event audit steps in order and keeps a trace of each. */
data class LegacyCameraEventAuditEngine(
    var steps: MutableList<LegacyCameraEventAuditModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyCameraEventAudit): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyCameraEventAuditModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyCameraEventAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyCameraEventAuditEngine(flags: Registry): LegacyCameraEventAuditEngine {
    return LegacyCameraEventAuditEngine(
        steps = mutableListOf<LegacyCameraEventAuditModifier>(
            RoundingLegacyCameraEventAuditAdjuster(5.0),
            FlaggedLegacyCameraEventAuditAdjuster(flags, "legacy-camera-event-audit-pending", -3.8),
            PercentLegacyCameraEventAuditAdjuster(-14.6),
            MinimumLegacyCameraEventAuditAdjuster(49.5),
            TitleLegacyCameraEventAuditAdjuster(
                mutableMapOf<String, Double>("east" to 1.05, "north" to 1.32, "central" to 0.74),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 78111.6,
    )
}
