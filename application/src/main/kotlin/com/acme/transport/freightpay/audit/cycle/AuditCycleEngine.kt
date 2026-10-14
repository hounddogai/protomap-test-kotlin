package com.acme.transport.freightpay.audit.cycle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the audit cycle calculation. */
interface AuditCycleModifier {
    fun describe(): String
    fun adjust(item: AuditCycle, value: Double): Double
}

data class CapAuditCycleAdjuster(var cap: Double) : AuditCycleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AuditCycle, value: Double): Double = minOf(value, cap)
}

data class PercentAuditCycleAdjuster(var percent: Double) : AuditCycleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AuditCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingAuditCycleAdjuster(var step: Double) : AuditCycleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AuditCycle, value: Double): Double = roundTo(value, step)
}

data class DepotAuditCycleAdjuster(var factors: MutableMap<String, Double>) : AuditCycleModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: AuditCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class AuditCycleStartsAtOffsetAdjuster(var weight: Double) : AuditCycleModifier {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: AuditCycle, value: Double): Double = value + item.startsAt.toDouble() * weight
}

/** Runs the audit cycle steps in order and keeps a trace of each. */
data class AuditCycleEngine(
    var steps: MutableList<AuditCycleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AuditCycle): Double {
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

    fun evaluateAll(items: MutableList<AuditCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAuditCycleEngine(): AuditCycleEngine {
    return AuditCycleEngine(
        steps = mutableListOf<AuditCycleModifier>(
            CapAuditCycleAdjuster(49920.0),
            PercentAuditCycleAdjuster(6.0),
            RoundingAuditCycleAdjuster(0.25),
            DepotAuditCycleAdjuster(mutableMapOf<String, Double>("west" to 1.24, "online" to 1.32)),
            AuditCycleStartsAtOffsetAdjuster(0.26),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 89874.3,
    )
}
