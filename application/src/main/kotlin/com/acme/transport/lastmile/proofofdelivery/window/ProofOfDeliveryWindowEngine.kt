package com.acme.transport.lastmile.proofofdelivery.window

/** One step of the proof of delivery window calculation. */
interface ProofOfDeliveryWindowModifier {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryWindow, value: Double): Double
}

data class CapProofOfDeliveryWindowAdjuster(var cap: Double) : ProofOfDeliveryWindowModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ProofOfDeliveryWindow, value: Double): Double = minOf(value, cap)
}

data class PercentProofOfDeliveryWindowAdjuster(var percent: Double) : ProofOfDeliveryWindowModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ProofOfDeliveryWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredProofOfDeliveryWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ProofOfDeliveryWindowModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ProofOfDeliveryWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ProofOfDeliveryWindowThresholdOffsetAdjuster(var weight: Double) : ProofOfDeliveryWindowModifier {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: ProofOfDeliveryWindow, value: Double): Double = value + item.threshold * weight
}

data class MinimumProofOfDeliveryWindowAdjuster(var minimum: Double) : ProofOfDeliveryWindowModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ProofOfDeliveryWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the proof of delivery window steps in order and keeps a trace of each. */
data class ProofOfDeliveryWindowEngine(
    var steps: MutableList<ProofOfDeliveryWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ProofOfDeliveryWindow): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ProofOfDeliveryWindowModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ProofOfDeliveryWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryWindowEngine(): ProofOfDeliveryWindowEngine {
    return ProofOfDeliveryWindowEngine(
        steps = mutableListOf<ProofOfDeliveryWindowModifier>(
            CapProofOfDeliveryWindowAdjuster(18968.0),
            PercentProofOfDeliveryWindowAdjuster(-13.5),
            TieredProofOfDeliveryWindowAdjuster(
                mutableListOf<Double>(1805.0, 3589.0),
                mutableListOf<Double>(0.91, 1.15),
            ),
            ProofOfDeliveryWindowThresholdOffsetAdjuster(0.67),
            MinimumProofOfDeliveryWindowAdjuster(47.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 27466.8,
    )
}
