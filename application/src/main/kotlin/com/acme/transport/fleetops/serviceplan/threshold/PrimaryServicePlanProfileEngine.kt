package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary service plan profile calculation. */
interface PrimaryServicePlanProfileFactor {
    fun describe(): String
    fun adjust(item: PrimaryServicePlanProfile, value: Double): Double
}

data class RoundingPrimaryServicePlanProfileAdjuster(var step: Double) : PrimaryServicePlanProfileFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryServicePlanProfile, value: Double): Double = roundTo(value, step)
}

data class FlaggedPrimaryServicePlanProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryServicePlanProfileFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryServicePlanProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentPrimaryServicePlanProfileAdjuster(var percent: Double) : PrimaryServicePlanProfileFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryServicePlanProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class PrimaryServicePlanProfileCapacityOffsetAdjuster(
    var weight: Double,
) : PrimaryServicePlanProfileFactor {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: PrimaryServicePlanProfile, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class CapPrimaryServicePlanProfileAdjuster(var cap: Double) : PrimaryServicePlanProfileFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryServicePlanProfile, value: Double): Double = minOf(value, cap)
}

/** Runs the primary service plan profile steps in order and keeps a trace of each. */
data class PrimaryServicePlanProfileEngine(
    var steps: MutableList<PrimaryServicePlanProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryServicePlanProfile): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryServicePlanProfileFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryServicePlanProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryServicePlanProfileEngine(flags: Registry): PrimaryServicePlanProfileEngine {
    return PrimaryServicePlanProfileEngine(
        steps = mutableListOf<PrimaryServicePlanProfileFactor>(
            RoundingPrimaryServicePlanProfileAdjuster(0.1),
            FlaggedPrimaryServicePlanProfileAdjuster(flags, "primary-service-plan-profile-primary", 7.6),
            PercentPrimaryServicePlanProfileAdjuster(-23.9),
            PrimaryServicePlanProfileCapacityOffsetAdjuster(1.22),
            CapPrimaryServicePlanProfileAdjuster(5049.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 10821.5,
    )
}
