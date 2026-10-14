package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

/** One step of the tiered proof of delivery manifest calculation. */
interface TieredProofOfDeliveryManifestAdjuster {
    fun describe(): String
    fun adjust(item: TieredProofOfDeliveryManifest, value: Double): Double
}

data class MinimumTieredProofOfDeliveryManifestAdjuster(
    var minimum: Double,
) : TieredProofOfDeliveryManifestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredProofOfDeliveryManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameTieredProofOfDeliveryManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredProofOfDeliveryManifestAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: TieredProofOfDeliveryManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredTieredProofOfDeliveryManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredProofOfDeliveryManifestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredProofOfDeliveryManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapTieredProofOfDeliveryManifestAdjuster(var cap: Double) : TieredProofOfDeliveryManifestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredProofOfDeliveryManifest, value: Double): Double = minOf(value, cap)
}

/** Runs the tiered proof of delivery manifest steps in order and keeps a trace of each. */
data class TieredProofOfDeliveryManifestEngine(
    var steps: MutableList<TieredProofOfDeliveryManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredProofOfDeliveryManifest): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredProofOfDeliveryManifestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TieredProofOfDeliveryManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredProofOfDeliveryManifestEngine(): TieredProofOfDeliveryManifestEngine {
    return TieredProofOfDeliveryManifestEngine(
        steps = mutableListOf<TieredProofOfDeliveryManifestAdjuster>(
            MinimumTieredProofOfDeliveryManifestAdjuster(35.1),
            NameTieredProofOfDeliveryManifestAdjuster(mutableMapOf<String, Double>("north" to 0.86)),
            TieredTieredProofOfDeliveryManifestAdjuster(
                mutableListOf<Double>(424.0, 4511.0, 8669.0, 9870.0),
                mutableListOf<Double>(1.01, 0.97, 1.06, 1.17),
            ),
            CapTieredProofOfDeliveryManifestAdjuster(40015.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 50861.7,
    )
}
