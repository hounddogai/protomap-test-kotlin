package com.acme.transport.freightpay.audit.manifest

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional audit window calculation. */
interface RegionalAuditWindowAdjuster {
    fun describe(): String
    fun adjust(item: RegionalAuditWindow, value: Double): Double
}

data class RegionalAuditWindowEndsAtOffsetAdjuster(var weight: Double) : RegionalAuditWindowAdjuster {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: RegionalAuditWindow, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class PercentRegionalAuditWindowAdjuster(var percent: Double) : RegionalAuditWindowAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalAuditWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRegionalAuditWindowAdjuster(var step: Double) : RegionalAuditWindowAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalAuditWindow, value: Double): Double = roundTo(value, step)
}

data class CapRegionalAuditWindowAdjuster(var cap: Double) : RegionalAuditWindowAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalAuditWindow, value: Double): Double = minOf(value, cap)
}

data class NameRegionalAuditWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalAuditWindowAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalAuditWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the regional audit window steps in order and keeps a trace of each. */
data class RegionalAuditWindowEngine(
    var steps: MutableList<RegionalAuditWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalAuditWindow): Double {
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

    fun evaluateAll(items: MutableList<RegionalAuditWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalAuditWindowEngine(): RegionalAuditWindowEngine {
    return RegionalAuditWindowEngine(
        steps = mutableListOf<RegionalAuditWindowAdjuster>(
            RegionalAuditWindowEndsAtOffsetAdjuster(1.52),
            PercentRegionalAuditWindowAdjuster(23.5),
            RoundingRegionalAuditWindowAdjuster(0.25),
            CapRegionalAuditWindowAdjuster(43218.0),
            NameRegionalAuditWindowAdjuster(
                mutableMapOf<String, Double>("west" to 0.76, "east" to 0.81, "online" to 0.74),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 44264.6,
    )
}
