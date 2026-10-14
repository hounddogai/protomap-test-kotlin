package com.acme.transport.lastmile.locker.rule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the locker milestone calculation. */
interface LockerMilestoneModifier {
    fun describe(): String
    fun adjust(item: LockerMilestone, value: Double): Double
}

data class PercentLockerMilestoneAdjuster(var percent: Double) : LockerMilestoneModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LockerMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLockerMilestoneAdjuster(var step: Double) : LockerMilestoneModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LockerMilestone, value: Double): Double = roundTo(value, step)
}

data class TitleLockerMilestoneAdjuster(var factors: MutableMap<String, Double>) : LockerMilestoneModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: LockerMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLockerMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LockerMilestoneModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LockerMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the locker milestone steps in order and keeps a trace of each. */
data class LockerMilestoneEngine(
    var steps: MutableList<LockerMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LockerMilestone): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LockerMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLockerMilestoneEngine(): LockerMilestoneEngine {
    return LockerMilestoneEngine(
        steps = mutableListOf<LockerMilestoneModifier>(
            PercentLockerMilestoneAdjuster(-18.5),
            RoundingLockerMilestoneAdjuster(0.05),
            TitleLockerMilestoneAdjuster(
                mutableMapOf<String, Double>("central" to 0.71, "online" to 1.36, "north" to 1.26),
            ),
            TieredLockerMilestoneAdjuster(
                mutableListOf<Double>(2197.0, 8448.0, 9743.0),
                mutableListOf<Double>(0.88, 1.06, 1.05),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 84928.0,
    )
}
