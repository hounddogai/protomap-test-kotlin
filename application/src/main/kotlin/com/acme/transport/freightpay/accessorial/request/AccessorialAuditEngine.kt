package com.acme.transport.freightpay.accessorial.request

import com.acme.transport.kit.numbers.roundTo

/** One step of the accessorial audit calculation. */
interface AccessorialAuditFactor {
    fun describe(): String
    fun adjust(item: AccessorialAudit, value: Double): Double
}

data class RoundingAccessorialAuditAdjuster(var step: Double) : AccessorialAuditFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AccessorialAudit, value: Double): Double = roundTo(value, step)
}

data class MinimumAccessorialAuditAdjuster(var minimum: Double) : AccessorialAuditFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AccessorialAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LocaleAccessorialAuditAdjuster(var factors: MutableMap<String, Double>) : AccessorialAuditFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: AccessorialAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredAccessorialAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AccessorialAuditFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AccessorialAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AccessorialAuditPalletsOffsetAdjuster(var weight: Double) : AccessorialAuditFactor {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: AccessorialAudit, value: Double): Double = value + item.pallets.toDouble() * weight
}

/** Runs the accessorial audit steps in order and keeps a trace of each. */
data class AccessorialAuditEngine(
    var steps: MutableList<AccessorialAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AccessorialAudit): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AccessorialAuditFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AccessorialAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAccessorialAuditEngine(): AccessorialAuditEngine {
    return AccessorialAuditEngine(
        steps = mutableListOf<AccessorialAuditFactor>(
            RoundingAccessorialAuditAdjuster(0.1),
            MinimumAccessorialAuditAdjuster(35.7),
            LocaleAccessorialAuditAdjuster(mutableMapOf<String, Double>("retail" to 1.15)),
            TieredAccessorialAuditAdjuster(
                mutableListOf<Double>(8076.0, 9625.0),
                mutableListOf<Double>(1.16, 1.08),
            ),
            AccessorialAuditPalletsOffsetAdjuster(0.58),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 43001.8,
    )
}
