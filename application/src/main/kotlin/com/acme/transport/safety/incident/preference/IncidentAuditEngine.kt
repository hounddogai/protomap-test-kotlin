package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the incident audit calculation. */
interface IncidentAuditStep {
    fun describe(): String
    fun adjust(item: IncidentAudit, value: Double): Double
}

data class PercentIncidentAuditAdjuster(var percent: Double) : IncidentAuditStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IncidentAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapIncidentAuditAdjuster(var cap: Double) : IncidentAuditStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IncidentAudit, value: Double): Double = minOf(value, cap)
}

data class IncidentAuditQuantityOffsetAdjuster(var weight: Double) : IncidentAuditStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: IncidentAudit, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class RoundingIncidentAuditAdjuster(var step: Double) : IncidentAuditStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IncidentAudit, value: Double): Double = roundTo(value, step)
}

data class FlaggedIncidentAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IncidentAuditStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IncidentAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CategoryIncidentAuditAdjuster(var factors: MutableMap<String, Double>) : IncidentAuditStep {
    override fun describe(): String = "by category"

    override fun adjust(item: IncidentAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the incident audit steps in order and keeps a trace of each. */
data class IncidentAuditEngine(
    var steps: MutableList<IncidentAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: IncidentAudit): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IncidentAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultIncidentAuditEngine(flags: Registry): IncidentAuditEngine {
    return IncidentAuditEngine(
        steps = mutableListOf<IncidentAuditStep>(
            PercentIncidentAuditAdjuster(6.3),
            CapIncidentAuditAdjuster(44987.0),
            IncidentAuditQuantityOffsetAdjuster(0.81),
            RoundingIncidentAuditAdjuster(0.5),
            FlaggedIncidentAuditAdjuster(flags, "incident-audit-primary", 7.9),
            CategoryIncidentAuditAdjuster(
                mutableMapOf<String, Double>("retail" to 0.8, "online" to 0.77, "north" to 1.34),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 73263.2,
    )
}
