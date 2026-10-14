package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.kit.numbers.roundTo

/** One step of the ev charge audit calculation. */
interface EvChargeAuditFactor {
    fun describe(): String
    fun adjust(item: EvChargeAudit, value: Double): Double
}

data class RoundingEvChargeAuditAdjuster(var step: Double) : EvChargeAuditFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EvChargeAudit, value: Double): Double = roundTo(value, step)
}

data class EvChargeAuditDurationMinutesOffsetAdjuster(var weight: Double) : EvChargeAuditFactor {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: EvChargeAudit, value: Double): Double = value + item.durationMinutes.toDouble() * weight
}

data class CapEvChargeAuditAdjuster(var cap: Double) : EvChargeAuditFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EvChargeAudit, value: Double): Double = minOf(value, cap)
}

data class PercentEvChargeAuditAdjuster(var percent: Double) : EvChargeAuditFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EvChargeAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the ev charge audit steps in order and keeps a trace of each. */
data class EvChargeAuditEngine(
    var steps: MutableList<EvChargeAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: EvChargeAudit): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EvChargeAuditFactor = steps[index]
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

    fun evaluateAll(items: MutableList<EvChargeAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultEvChargeAuditEngine(): EvChargeAuditEngine {
    return EvChargeAuditEngine(
        steps = mutableListOf<EvChargeAuditFactor>(
            RoundingEvChargeAuditAdjuster(0.25),
            EvChargeAuditDurationMinutesOffsetAdjuster(1.84),
            CapEvChargeAuditAdjuster(37095.0),
            PercentEvChargeAuditAdjuster(24.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 23979.2,
    )
}
