package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient dispute audit calculation. */
interface LenientDisputeAuditModifier {
    fun describe(): String
    fun adjust(item: LenientDisputeAudit, value: Double): Double
}

data class RoundingLenientDisputeAuditAdjuster(var step: Double) : LenientDisputeAuditModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientDisputeAudit, value: Double): Double = roundTo(value, step)
}

data class CapLenientDisputeAuditAdjuster(var cap: Double) : LenientDisputeAuditModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientDisputeAudit, value: Double): Double = minOf(value, cap)
}

data class TieredLenientDisputeAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientDisputeAuditModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientDisputeAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LenientDisputeAuditFuelLitersOffsetAdjuster(var weight: Double) : LenientDisputeAuditModifier {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: LenientDisputeAudit, value: Double): Double = value + item.fuelLiters * weight
}

data class PercentLenientDisputeAuditAdjuster(var percent: Double) : LenientDisputeAuditModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientDisputeAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleLenientDisputeAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientDisputeAuditModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: LenientDisputeAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient dispute audit steps in order and keeps a trace of each. */
data class LenientDisputeAuditEngine(
    var steps: MutableList<LenientDisputeAuditModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientDisputeAudit): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientDisputeAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientDisputeAuditEngine(): LenientDisputeAuditEngine {
    return LenientDisputeAuditEngine(
        steps = mutableListOf<LenientDisputeAuditModifier>(
            RoundingLenientDisputeAuditAdjuster(0.25),
            CapLenientDisputeAuditAdjuster(30145.0),
            TieredLenientDisputeAuditAdjuster(
                mutableListOf<Double>(892.0, 4319.0, 5663.0, 6497.0),
                mutableListOf<Double>(1.11, 0.91, 0.84, 0.82),
            ),
            LenientDisputeAuditFuelLitersOffsetAdjuster(1.67),
            PercentLenientDisputeAuditAdjuster(18.1),
            TitleLenientDisputeAuditAdjuster(mutableMapOf<String, Double>("central" to 1.3, "retail" to 1.4)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 46043.2,
    )
}
