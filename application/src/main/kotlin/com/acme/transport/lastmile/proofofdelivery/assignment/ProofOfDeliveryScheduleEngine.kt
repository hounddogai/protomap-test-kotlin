package com.acme.transport.lastmile.proofofdelivery.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the proof of delivery schedule calculation. */
interface ProofOfDeliveryScheduleFactor {
    fun describe(): String
    fun adjust(item: ProofOfDeliverySchedule, value: Double): Double
}

data class RoundingProofOfDeliveryScheduleAdjuster(var step: Double) : ProofOfDeliveryScheduleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ProofOfDeliverySchedule, value: Double): Double = roundTo(value, step)
}

data class FlaggedProofOfDeliveryScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ProofOfDeliveryScheduleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ProofOfDeliverySchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredProofOfDeliveryScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ProofOfDeliveryScheduleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ProofOfDeliverySchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the proof of delivery schedule steps in order and keeps a trace of each. */
data class ProofOfDeliveryScheduleEngine(
    var steps: MutableList<ProofOfDeliveryScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ProofOfDeliverySchedule): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ProofOfDeliveryScheduleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ProofOfDeliverySchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryScheduleEngine(flags: Registry): ProofOfDeliveryScheduleEngine {
    return ProofOfDeliveryScheduleEngine(
        steps = mutableListOf<ProofOfDeliveryScheduleFactor>(
            RoundingProofOfDeliveryScheduleAdjuster(0.1),
            FlaggedProofOfDeliveryScheduleAdjuster(flags, "proof-of-delivery-schedule-manual", -1.3),
            TieredProofOfDeliveryScheduleAdjuster(
                mutableListOf<Double>(4290.0, 6667.0, 7360.0, 8985.0),
                mutableListOf<Double>(0.81, 1.12, 1.02, 0.94),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 5697.9,
    )
}
