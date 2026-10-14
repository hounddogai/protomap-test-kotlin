package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the audit rollup calculation. */
interface AuditRollupAdjuster {
    fun describe(): String
    fun adjust(item: AuditRollup, value: Double): Double
}

data class LocaleAuditRollupAdjuster(var factors: MutableMap<String, Double>) : AuditRollupAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: AuditRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAuditRollupAdjuster(var cap: Double) : AuditRollupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AuditRollup, value: Double): Double = minOf(value, cap)
}

data class RoundingAuditRollupAdjuster(var step: Double) : AuditRollupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AuditRollup, value: Double): Double = roundTo(value, step)
}

data class TieredAuditRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AuditRollupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AuditRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAuditRollupAdjuster(var minimum: Double) : AuditRollupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AuditRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the audit rollup steps in order and keeps a trace of each. */
data class AuditRollupEngine(
    var steps: MutableList<AuditRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AuditRollup): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AuditRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAuditRollupEngine(): AuditRollupEngine {
    return AuditRollupEngine(
        steps = mutableListOf<AuditRollupAdjuster>(
            LocaleAuditRollupAdjuster(mutableMapOf<String, Double>("north" to 1.15, "online" to 1.01)),
            CapAuditRollupAdjuster(27641.0),
            RoundingAuditRollupAdjuster(0.5),
            TieredAuditRollupAdjuster(
                mutableListOf<Double>(4366.0, 4892.0, 6221.0, 7857.0),
                mutableListOf<Double>(1.1, 1.05, 0.84, 1.11),
            ),
            MinimumAuditRollupAdjuster(17.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 6996.1,
    )
}
