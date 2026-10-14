package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled proof of delivery policy calculation. */
interface ScheduledProofOfDeliveryPolicyModifier {
    fun describe(): String
    fun adjust(item: ScheduledProofOfDeliveryPolicy, value: Double): Double
}

data class PercentScheduledProofOfDeliveryPolicyAdjuster(
    var percent: Double,
) : ScheduledProofOfDeliveryPolicyModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledProofOfDeliveryPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapScheduledProofOfDeliveryPolicyAdjuster(var cap: Double) : ScheduledProofOfDeliveryPolicyModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledProofOfDeliveryPolicy, value: Double): Double = minOf(value, cap)
}

data class ScheduledProofOfDeliveryPolicyAttemptsOffsetAdjuster(
    var weight: Double,
) : ScheduledProofOfDeliveryPolicyModifier {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: ScheduledProofOfDeliveryPolicy, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

/** Runs the scheduled proof of delivery policy steps in order and keeps a trace of each. */
data class ScheduledProofOfDeliveryPolicyEngine(
    var steps: MutableList<ScheduledProofOfDeliveryPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledProofOfDeliveryPolicy): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledProofOfDeliveryPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledProofOfDeliveryPolicyEngine(): ScheduledProofOfDeliveryPolicyEngine {
    return ScheduledProofOfDeliveryPolicyEngine(
        steps = mutableListOf<ScheduledProofOfDeliveryPolicyModifier>(
            PercentScheduledProofOfDeliveryPolicyAdjuster(-21.0),
            CapScheduledProofOfDeliveryPolicyAdjuster(23484.0),
            ScheduledProofOfDeliveryPolicyAttemptsOffsetAdjuster(1.58),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 72305.7,
    )
}
