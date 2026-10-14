package com.acme.transport.linehaul.tender.profile

import com.acme.transport.kit.numbers.roundTo

/** One step of the tender quota calculation. */
interface TenderQuotaFactor {
    fun describe(): String
    fun adjust(item: TenderQuota, value: Double): Double
}

data class RoundingTenderQuotaAdjuster(var step: Double) : TenderQuotaFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TenderQuota, value: Double): Double = roundTo(value, step)
}

data class PercentTenderQuotaAdjuster(var percent: Double) : TenderQuotaFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TenderQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapTenderQuotaAdjuster(var cap: Double) : TenderQuotaFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TenderQuota, value: Double): Double = minOf(value, cap)
}

data class LabelTenderQuotaAdjuster(var factors: MutableMap<String, Double>) : TenderQuotaFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: TenderQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tender quota steps in order and keeps a trace of each. */
data class TenderQuotaEngine(
    var steps: MutableList<TenderQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TenderQuota): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TenderQuotaFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TenderQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTenderQuotaEngine(): TenderQuotaEngine {
    return TenderQuotaEngine(
        steps = mutableListOf<TenderQuotaFactor>(
            RoundingTenderQuotaAdjuster(0.25),
            PercentTenderQuotaAdjuster(8.5),
            CapTenderQuotaAdjuster(6115.0),
            LabelTenderQuotaAdjuster(mutableMapOf<String, Double>("central" to 0.9, "retail" to 1.26)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 81677.7,
    )
}
