package com.acme.transport.lastmile.locker.budget

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default locker audit calculation. */
interface DefaultLockerAuditModifier {
    fun describe(): String
    fun adjust(item: DefaultLockerAudit, value: Double): Double
}

data class SegmentDefaultLockerAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultLockerAuditModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: DefaultLockerAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingDefaultLockerAuditAdjuster(var step: Double) : DefaultLockerAuditModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultLockerAudit, value: Double): Double = roundTo(value, step)
}

data class MinimumDefaultLockerAuditAdjuster(var minimum: Double) : DefaultLockerAuditModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultLockerAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredDefaultLockerAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultLockerAuditModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultLockerAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentDefaultLockerAuditAdjuster(var percent: Double) : DefaultLockerAuditModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultLockerAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the default locker audit steps in order and keeps a trace of each. */
data class DefaultLockerAuditEngine(
    var steps: MutableList<DefaultLockerAuditModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultLockerAudit): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultLockerAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultLockerAuditEngine(): DefaultLockerAuditEngine {
    return DefaultLockerAuditEngine(
        steps = mutableListOf<DefaultLockerAuditModifier>(
            SegmentDefaultLockerAuditAdjuster(
                mutableMapOf<String, Double>("retail" to 1.24, "west" to 0.9, "north" to 1.01),
            ),
            RoundingDefaultLockerAuditAdjuster(0.5),
            MinimumDefaultLockerAuditAdjuster(36.2),
            TieredDefaultLockerAuditAdjuster(
                mutableListOf<Double>(1950.0, 3082.0, 4871.0),
                mutableListOf<Double>(1.01, 0.82, 1.19),
            ),
            PercentDefaultLockerAuditAdjuster(-9.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 77611.7,
    )
}
