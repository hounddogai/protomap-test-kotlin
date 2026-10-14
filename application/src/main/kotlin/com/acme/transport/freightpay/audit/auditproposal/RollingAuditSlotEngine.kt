package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling audit slot calculation. */
interface RollingAuditSlotStep {
    fun describe(): String
    fun adjust(item: RollingAuditSlot, value: Double): Double
}

data class PercentRollingAuditSlotAdjuster(var percent: Double) : RollingAuditSlotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingAuditSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRollingAuditSlotAdjuster(var step: Double) : RollingAuditSlotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingAuditSlot, value: Double): Double = roundTo(value, step)
}

data class TieredRollingAuditSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingAuditSlotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingAuditSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rolling audit slot steps in order and keeps a trace of each. */
data class RollingAuditSlotEngine(
    var steps: MutableList<RollingAuditSlotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingAuditSlot): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingAuditSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingAuditSlotEngine(): RollingAuditSlotEngine {
    return RollingAuditSlotEngine(
        steps = mutableListOf<RollingAuditSlotStep>(
            PercentRollingAuditSlotAdjuster(14.8),
            RoundingRollingAuditSlotAdjuster(1.0),
            TieredRollingAuditSlotAdjuster(
                mutableListOf<Double>(3260.0, 9108.0, 9237.0),
                mutableListOf<Double>(0.8, 1.16, 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 53162.2,
    )
}
