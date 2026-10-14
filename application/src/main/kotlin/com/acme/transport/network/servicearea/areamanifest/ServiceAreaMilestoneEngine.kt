package com.acme.transport.network.servicearea.areamanifest

import com.acme.transport.kit.numbers.roundTo

/** One step of the service area milestone calculation. */
interface ServiceAreaMilestoneFactor {
    fun describe(): String
    fun adjust(item: ServiceAreaMilestone, value: Double): Double
}

data class CapServiceAreaMilestoneAdjuster(var cap: Double) : ServiceAreaMilestoneFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServiceAreaMilestone, value: Double): Double = minOf(value, cap)
}

data class TitleServiceAreaMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : ServiceAreaMilestoneFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: ServiceAreaMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ServiceAreaMilestoneBudgetOffsetAdjuster(var weight: Double) : ServiceAreaMilestoneFactor {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: ServiceAreaMilestone, value: Double): Double = value + item.budget * weight
}

data class PercentServiceAreaMilestoneAdjuster(var percent: Double) : ServiceAreaMilestoneFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServiceAreaMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingServiceAreaMilestoneAdjuster(var step: Double) : ServiceAreaMilestoneFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServiceAreaMilestone, value: Double): Double = roundTo(value, step)
}

/** Runs the service area milestone steps in order and keeps a trace of each. */
data class ServiceAreaMilestoneEngine(
    var steps: MutableList<ServiceAreaMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ServiceAreaMilestone): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServiceAreaMilestoneFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ServiceAreaMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultServiceAreaMilestoneEngine(): ServiceAreaMilestoneEngine {
    return ServiceAreaMilestoneEngine(
        steps = mutableListOf<ServiceAreaMilestoneFactor>(
            CapServiceAreaMilestoneAdjuster(38377.0),
            TitleServiceAreaMilestoneAdjuster(mutableMapOf<String, Double>("retail" to 0.7, "north" to 1.36)),
            ServiceAreaMilestoneBudgetOffsetAdjuster(1.02),
            PercentServiceAreaMilestoneAdjuster(6.2),
            RoundingServiceAreaMilestoneAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 68559.7,
    )
}
