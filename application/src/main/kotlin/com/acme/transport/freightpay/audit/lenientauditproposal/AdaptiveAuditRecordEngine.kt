package com.acme.transport.freightpay.audit.lenientauditproposal

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive audit record calculation. */
interface AdaptiveAuditRecordFactor {
    fun describe(): String
    fun adjust(item: AdaptiveAuditRecord, value: Double): Double
}

data class TieredAdaptiveAuditRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveAuditRecordFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveAuditRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelAdaptiveAuditRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveAuditRecordFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: AdaptiveAuditRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAdaptiveAuditRecordAdjuster(var cap: Double) : AdaptiveAuditRecordFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveAuditRecord, value: Double): Double = minOf(value, cap)
}

data class PercentAdaptiveAuditRecordAdjuster(var percent: Double) : AdaptiveAuditRecordFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveAuditRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingAdaptiveAuditRecordAdjuster(var step: Double) : AdaptiveAuditRecordFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveAuditRecord, value: Double): Double = roundTo(value, step)
}

data class AdaptiveAuditRecordEtaMinutesOffsetAdjuster(var weight: Double) : AdaptiveAuditRecordFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: AdaptiveAuditRecord, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

/** Runs the adaptive audit record steps in order and keeps a trace of each. */
data class AdaptiveAuditRecordEngine(
    var steps: MutableList<AdaptiveAuditRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveAuditRecord): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveAuditRecordFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveAuditRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveAuditRecordEngine(): AdaptiveAuditRecordEngine {
    return AdaptiveAuditRecordEngine(
        steps = mutableListOf<AdaptiveAuditRecordFactor>(
            TieredAdaptiveAuditRecordAdjuster(
                mutableListOf<Double>(171.0, 1578.0, 5182.0, 9585.0),
                mutableListOf<Double>(1.08, 1.2, 1.15, 1.14),
            ),
            LabelAdaptiveAuditRecordAdjuster(mutableMapOf<String, Double>("online" to 1.18)),
            CapAdaptiveAuditRecordAdjuster(43899.0),
            PercentAdaptiveAuditRecordAdjuster(-15.3),
            RoundingAdaptiveAuditRecordAdjuster(5.0),
            AdaptiveAuditRecordEtaMinutesOffsetAdjuster(1.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 98112.5,
    )
}
