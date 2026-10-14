package com.acme.transport.linehaul.tender.tenderestimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tender audit calculation. */
interface TenderAuditFactor {
    fun describe(): String
    fun adjust(item: TenderAudit, value: Double): Double
}

data class FlaggedTenderAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TenderAuditFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TenderAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingTenderAuditAdjuster(var step: Double) : TenderAuditFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TenderAudit, value: Double): Double = roundTo(value, step)
}

data class TenderAuditCapacityOffsetAdjuster(var weight: Double) : TenderAuditFactor {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: TenderAudit, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class CapTenderAuditAdjuster(var cap: Double) : TenderAuditFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TenderAudit, value: Double): Double = minOf(value, cap)
}

data class PercentTenderAuditAdjuster(var percent: Double) : TenderAuditFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TenderAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tender audit steps in order and keeps a trace of each. */
data class TenderAuditEngine(
    var steps: MutableList<TenderAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TenderAudit): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TenderAuditFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TenderAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTenderAuditEngine(flags: Registry): TenderAuditEngine {
    return TenderAuditEngine(
        steps = mutableListOf<TenderAuditFactor>(
            FlaggedTenderAuditAdjuster(flags, "tender-audit-scheduled", 6.0),
            RoundingTenderAuditAdjuster(0.25),
            TenderAuditCapacityOffsetAdjuster(1.75),
            CapTenderAuditAdjuster(45049.0),
            PercentTenderAuditAdjuster(-11.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 60110.7,
    )
}
