package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.kit.flags.Registry

/** One step of the proof of delivery plan calculation. */
interface ProofOfDeliveryPlanStep {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryPlan, value: Double): Double
}

data class ProofOfDeliveryPlanStopCountOffsetAdjuster(var weight: Double) : ProofOfDeliveryPlanStep {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: ProofOfDeliveryPlan, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class TieredProofOfDeliveryPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ProofOfDeliveryPlanStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ProofOfDeliveryPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleProofOfDeliveryPlanAdjuster(var factors: MutableMap<String, Double>) : ProofOfDeliveryPlanStep {
    override fun describe(): String = "by title"

    override fun adjust(item: ProofOfDeliveryPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapProofOfDeliveryPlanAdjuster(var cap: Double) : ProofOfDeliveryPlanStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ProofOfDeliveryPlan, value: Double): Double = minOf(value, cap)
}

data class FlaggedProofOfDeliveryPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ProofOfDeliveryPlanStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ProofOfDeliveryPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the proof of delivery plan steps in order and keeps a trace of each. */
data class ProofOfDeliveryPlanEngine(
    var steps: MutableList<ProofOfDeliveryPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ProofOfDeliveryPlan): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ProofOfDeliveryPlanStep = steps[index]
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

    fun evaluateAll(items: MutableList<ProofOfDeliveryPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryPlanEngine(flags: Registry): ProofOfDeliveryPlanEngine {
    return ProofOfDeliveryPlanEngine(
        steps = mutableListOf<ProofOfDeliveryPlanStep>(
            ProofOfDeliveryPlanStopCountOffsetAdjuster(1.07),
            TieredProofOfDeliveryPlanAdjuster(
                mutableListOf<Double>(2619.0, 6816.0),
                mutableListOf<Double>(0.98, 1.03),
            ),
            TitleProofOfDeliveryPlanAdjuster(
                mutableMapOf<String, Double>("north" to 1.07, "south" to 0.76, "online" to 1.04),
            ),
            CapProofOfDeliveryPlanAdjuster(35860.0),
            FlaggedProofOfDeliveryPlanAdjuster(flags, "proof-of-delivery-plan-fallback", -4.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 12307.7,
    )
}
