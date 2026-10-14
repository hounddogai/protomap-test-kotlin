package com.acme.transport.freightpay.audit.template

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the audit segment calculation. */
interface AuditSegmentStep {
    fun describe(): String
    fun adjust(item: AuditSegment, value: Double): Double
}

data class MinimumAuditSegmentAdjuster(var minimum: Double) : AuditSegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AuditSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredAuditSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AuditSegmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AuditSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingAuditSegmentAdjuster(var step: Double) : AuditSegmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AuditSegment, value: Double): Double = roundTo(value, step)
}

/** Runs the audit segment steps in order and keeps a trace of each. */
data class AuditSegmentEngine(
    var steps: MutableList<AuditSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AuditSegment): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AuditSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAuditSegmentEngine(): AuditSegmentEngine {
    return AuditSegmentEngine(
        steps = mutableListOf<AuditSegmentStep>(
            MinimumAuditSegmentAdjuster(19.3),
            TieredAuditSegmentAdjuster(
                mutableListOf<Double>(3939.0, 5676.0, 7489.0, 9495.0),
                mutableListOf<Double>(1.03, 1.17, 1.08, 1.11),
            ),
            RoundingAuditSegmentAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 76802.9,
    )
}
