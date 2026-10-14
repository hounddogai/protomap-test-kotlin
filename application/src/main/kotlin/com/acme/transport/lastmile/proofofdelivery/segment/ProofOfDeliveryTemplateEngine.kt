package com.acme.transport.lastmile.proofofdelivery.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the proof of delivery template calculation. */
interface ProofOfDeliveryTemplateStep {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryTemplate, value: Double): Double
}

data class ProofOfDeliveryTemplateWindowDaysOffsetAdjuster(var weight: Double) : ProofOfDeliveryTemplateStep {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: ProofOfDeliveryTemplate, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class PercentProofOfDeliveryTemplateAdjuster(var percent: Double) : ProofOfDeliveryTemplateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ProofOfDeliveryTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapProofOfDeliveryTemplateAdjuster(var cap: Double) : ProofOfDeliveryTemplateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ProofOfDeliveryTemplate, value: Double): Double = minOf(value, cap)
}

data class FlaggedProofOfDeliveryTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ProofOfDeliveryTemplateStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ProofOfDeliveryTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the proof of delivery template steps in order and keeps a trace of each. */
data class ProofOfDeliveryTemplateEngine(
    var steps: MutableList<ProofOfDeliveryTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ProofOfDeliveryTemplate): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ProofOfDeliveryTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryTemplateEngine(flags: Registry): ProofOfDeliveryTemplateEngine {
    return ProofOfDeliveryTemplateEngine(
        steps = mutableListOf<ProofOfDeliveryTemplateStep>(
            ProofOfDeliveryTemplateWindowDaysOffsetAdjuster(1.15),
            PercentProofOfDeliveryTemplateAdjuster(23.9),
            CapProofOfDeliveryTemplateAdjuster(41487.0),
            FlaggedProofOfDeliveryTemplateAdjuster(flags, "proof-of-delivery-template-default", 0.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 49957.4,
    )
}
