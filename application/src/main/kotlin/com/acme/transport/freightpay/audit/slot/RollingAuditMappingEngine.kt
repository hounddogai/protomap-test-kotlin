package com.acme.transport.freightpay.audit.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling audit mapping calculation. */
interface RollingAuditMappingAdjuster {
    fun describe(): String
    fun adjust(item: RollingAuditMapping, value: Double): Double
}

data class CapRollingAuditMappingAdjuster(var cap: Double) : RollingAuditMappingAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingAuditMapping, value: Double): Double = minOf(value, cap)
}

data class NameRollingAuditMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingAuditMappingAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: RollingAuditMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRollingAuditMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingAuditMappingAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingAuditMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RollingAuditMappingThresholdOffsetAdjuster(var weight: Double) : RollingAuditMappingAdjuster {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: RollingAuditMapping, value: Double): Double = value + item.threshold * weight
}

data class RoundingRollingAuditMappingAdjuster(var step: Double) : RollingAuditMappingAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingAuditMapping, value: Double): Double = roundTo(value, step)
}

data class TieredRollingAuditMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingAuditMappingAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingAuditMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rolling audit mapping steps in order and keeps a trace of each. */
data class RollingAuditMappingEngine(
    var steps: MutableList<RollingAuditMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingAuditMapping): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingAuditMappingAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingAuditMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingAuditMappingEngine(flags: Registry): RollingAuditMappingEngine {
    return RollingAuditMappingEngine(
        steps = mutableListOf<RollingAuditMappingAdjuster>(
            CapRollingAuditMappingAdjuster(18559.0),
            NameRollingAuditMappingAdjuster(mutableMapOf<String, Double>("east" to 0.9)),
            FlaggedRollingAuditMappingAdjuster(flags, "rolling-audit-mapping-batched", 0.2),
            RollingAuditMappingThresholdOffsetAdjuster(1.43),
            RoundingRollingAuditMappingAdjuster(0.05),
            TieredRollingAuditMappingAdjuster(
                mutableListOf<Double>(3979.0, 4373.0),
                mutableListOf<Double>(1.2, 0.86),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 1091.9,
    )
}
