package com.acme.transport.freightpay.accessorial.audit

import com.acme.transport.kit.numbers.roundTo

/** One step of the capped accessorial audit calculation. */
interface CappedAccessorialAuditFactor {
    fun describe(): String
    fun adjust(item: CappedAccessorialAudit, value: Double): Double
}

data class MinimumCappedAccessorialAuditAdjuster(var minimum: Double) : CappedAccessorialAuditFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedAccessorialAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CappedAccessorialAuditAmountOffsetAdjuster(var weight: Double) : CappedAccessorialAuditFactor {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: CappedAccessorialAudit, value: Double): Double = value + item.amount * weight
}

data class RoundingCappedAccessorialAuditAdjuster(var step: Double) : CappedAccessorialAuditFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedAccessorialAudit, value: Double): Double = roundTo(value, step)
}

data class LabelCappedAccessorialAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedAccessorialAuditFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: CappedAccessorialAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCappedAccessorialAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedAccessorialAuditFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedAccessorialAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCappedAccessorialAuditAdjuster(var percent: Double) : CappedAccessorialAuditFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedAccessorialAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capped accessorial audit steps in order and keeps a trace of each. */
data class CappedAccessorialAuditEngine(
    var steps: MutableList<CappedAccessorialAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedAccessorialAudit): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedAccessorialAuditFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CappedAccessorialAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedAccessorialAuditEngine(): CappedAccessorialAuditEngine {
    return CappedAccessorialAuditEngine(
        steps = mutableListOf<CappedAccessorialAuditFactor>(
            MinimumCappedAccessorialAuditAdjuster(34.2),
            CappedAccessorialAuditAmountOffsetAdjuster(0.33),
            RoundingCappedAccessorialAuditAdjuster(0.5),
            LabelCappedAccessorialAuditAdjuster(mutableMapOf<String, Double>("online" to 1.14)),
            TieredCappedAccessorialAuditAdjuster(
                mutableListOf<Double>(8208.0, 8931.0, 9259.0),
                mutableListOf<Double>(1.06, 1.05, 1.17),
            ),
            PercentCappedAccessorialAuditAdjuster(15.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 38649.3,
    )
}
