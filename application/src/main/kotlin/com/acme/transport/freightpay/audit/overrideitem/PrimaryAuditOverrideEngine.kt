package com.acme.transport.freightpay.audit.overrideitem

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary audit override calculation. */
interface PrimaryAuditOverrideStep {
    fun describe(): String
    fun adjust(item: PrimaryAuditOverride, value: Double): Double
}

data class MinimumPrimaryAuditOverrideAdjuster(var minimum: Double) : PrimaryAuditOverrideStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryAuditOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingPrimaryAuditOverrideAdjuster(var step: Double) : PrimaryAuditOverrideStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryAuditOverride, value: Double): Double = roundTo(value, step)
}

data class PrimaryAuditOverrideDurationMinutesOffsetAdjuster(var weight: Double) : PrimaryAuditOverrideStep {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: PrimaryAuditOverride, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class CategoryPrimaryAuditOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryAuditOverrideStep {
    override fun describe(): String = "by category"

    override fun adjust(item: PrimaryAuditOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary audit override steps in order and keeps a trace of each. */
data class PrimaryAuditOverrideEngine(
    var steps: MutableList<PrimaryAuditOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryAuditOverride): Double {
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

    fun evaluateAll(items: MutableList<PrimaryAuditOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryAuditOverrideEngine(): PrimaryAuditOverrideEngine {
    return PrimaryAuditOverrideEngine(
        steps = mutableListOf<PrimaryAuditOverrideStep>(
            MinimumPrimaryAuditOverrideAdjuster(44.3),
            RoundingPrimaryAuditOverrideAdjuster(0.5),
            PrimaryAuditOverrideDurationMinutesOffsetAdjuster(0.81),
            CategoryPrimaryAuditOverrideAdjuster(
                mutableMapOf<String, Double>("central" to 1.4, "north" to 0.73),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 73431.6,
    )
}
