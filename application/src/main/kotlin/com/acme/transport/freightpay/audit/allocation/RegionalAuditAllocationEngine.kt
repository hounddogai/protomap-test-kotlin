package com.acme.transport.freightpay.audit.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional audit allocation calculation. */
interface RegionalAuditAllocationFactor {
    fun describe(): String
    fun adjust(item: RegionalAuditAllocation, value: Double): Double
}

data class RoundingRegionalAuditAllocationAdjuster(var step: Double) : RegionalAuditAllocationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalAuditAllocation, value: Double): Double = roundTo(value, step)
}

data class FlaggedRegionalAuditAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalAuditAllocationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalAuditAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRegionalAuditAllocationAdjuster(var cap: Double) : RegionalAuditAllocationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalAuditAllocation, value: Double): Double = minOf(value, cap)
}

data class RegionalAuditAllocationQuantityOffsetAdjuster(var weight: Double) : RegionalAuditAllocationFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: RegionalAuditAllocation, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class TieredRegionalAuditAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalAuditAllocationFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalAuditAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the regional audit allocation steps in order and keeps a trace of each. */
data class RegionalAuditAllocationEngine(
    var steps: MutableList<RegionalAuditAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalAuditAllocation): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalAuditAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalAuditAllocationEngine(flags: Registry): RegionalAuditAllocationEngine {
    return RegionalAuditAllocationEngine(
        steps = mutableListOf<RegionalAuditAllocationFactor>(
            RoundingRegionalAuditAllocationAdjuster(1.0),
            FlaggedRegionalAuditAllocationAdjuster(flags, "regional-audit-allocation-primary", 0.3),
            CapRegionalAuditAllocationAdjuster(39886.0),
            RegionalAuditAllocationQuantityOffsetAdjuster(0.87),
            TieredRegionalAuditAllocationAdjuster(
                mutableListOf<Double>(3544.0, 4133.0, 7138.0),
                mutableListOf<Double>(1.02, 1.14, 1.12),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 54940.4,
    )
}
