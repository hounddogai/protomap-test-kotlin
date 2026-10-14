package com.acme.transport.safety.incident.bundle

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary incident quota calculation. */
interface PrimaryIncidentQuotaStep {
    fun describe(): String
    fun adjust(item: PrimaryIncidentQuota, value: Double): Double
}

data class CapPrimaryIncidentQuotaAdjuster(var cap: Double) : PrimaryIncidentQuotaStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryIncidentQuota, value: Double): Double = minOf(value, cap)
}

data class RoundingPrimaryIncidentQuotaAdjuster(var step: Double) : PrimaryIncidentQuotaStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryIncidentQuota, value: Double): Double = roundTo(value, step)
}

data class MinimumPrimaryIncidentQuotaAdjuster(var minimum: Double) : PrimaryIncidentQuotaStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryIncidentQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PrimaryIncidentQuotaAmountOffsetAdjuster(var weight: Double) : PrimaryIncidentQuotaStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: PrimaryIncidentQuota, value: Double): Double = value + item.amount * weight
}

data class PercentPrimaryIncidentQuotaAdjuster(var percent: Double) : PrimaryIncidentQuotaStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryIncidentQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the primary incident quota steps in order and keeps a trace of each. */
data class PrimaryIncidentQuotaEngine(
    var steps: MutableList<PrimaryIncidentQuotaStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryIncidentQuota): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryIncidentQuotaStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryIncidentQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryIncidentQuotaEngine(): PrimaryIncidentQuotaEngine {
    return PrimaryIncidentQuotaEngine(
        steps = mutableListOf<PrimaryIncidentQuotaStep>(
            CapPrimaryIncidentQuotaAdjuster(30362.0),
            RoundingPrimaryIncidentQuotaAdjuster(0.05),
            MinimumPrimaryIncidentQuotaAdjuster(6.5),
            PrimaryIncidentQuotaAmountOffsetAdjuster(1.63),
            PercentPrimaryIncidentQuotaAdjuster(14.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 85753.7,
    )
}
