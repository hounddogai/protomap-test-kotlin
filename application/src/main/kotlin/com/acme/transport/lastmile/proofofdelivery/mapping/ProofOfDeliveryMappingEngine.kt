package com.acme.transport.lastmile.proofofdelivery.mapping

/** One step of the proof of delivery mapping calculation. */
interface ProofOfDeliveryMappingFactor {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryMapping, value: Double): Double
}

data class CapProofOfDeliveryMappingAdjuster(var cap: Double) : ProofOfDeliveryMappingFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ProofOfDeliveryMapping, value: Double): Double = minOf(value, cap)
}

data class MinimumProofOfDeliveryMappingAdjuster(var minimum: Double) : ProofOfDeliveryMappingFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ProofOfDeliveryMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ProofOfDeliveryMappingAmountOffsetAdjuster(var weight: Double) : ProofOfDeliveryMappingFactor {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: ProofOfDeliveryMapping, value: Double): Double = value + item.amount * weight
}

data class PercentProofOfDeliveryMappingAdjuster(var percent: Double) : ProofOfDeliveryMappingFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ProofOfDeliveryMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the proof of delivery mapping steps in order and keeps a trace of each. */
data class ProofOfDeliveryMappingEngine(
    var steps: MutableList<ProofOfDeliveryMappingFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ProofOfDeliveryMapping): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ProofOfDeliveryMappingFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ProofOfDeliveryMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryMappingEngine(): ProofOfDeliveryMappingEngine {
    return ProofOfDeliveryMappingEngine(
        steps = mutableListOf<ProofOfDeliveryMappingFactor>(
            CapProofOfDeliveryMappingAdjuster(39544.0),
            MinimumProofOfDeliveryMappingAdjuster(22.8),
            ProofOfDeliveryMappingAmountOffsetAdjuster(1.79),
            PercentProofOfDeliveryMappingAdjuster(11.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 27630.1,
    )
}
