package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual proof of delivery claim calculation. */
interface ManualProofOfDeliveryClaimFactor {
    fun describe(): String
    fun adjust(item: ManualProofOfDeliveryClaim, value: Double): Double
}

data class ManualProofOfDeliveryClaimDistanceKmOffsetAdjuster(
    var weight: Double,
) : ManualProofOfDeliveryClaimFactor {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: ManualProofOfDeliveryClaim, value: Double): Double = value + item.distanceKm * weight
}

data class DepotManualProofOfDeliveryClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualProofOfDeliveryClaimFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: ManualProofOfDeliveryClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedManualProofOfDeliveryClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualProofOfDeliveryClaimFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualProofOfDeliveryClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the manual proof of delivery claim steps in order and keeps a trace of each. */
data class ManualProofOfDeliveryClaimEngine(
    var steps: MutableList<ManualProofOfDeliveryClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualProofOfDeliveryClaim): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualProofOfDeliveryClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualProofOfDeliveryClaimEngine(flags: Registry): ManualProofOfDeliveryClaimEngine {
    return ManualProofOfDeliveryClaimEngine(
        steps = mutableListOf<ManualProofOfDeliveryClaimFactor>(
            ManualProofOfDeliveryClaimDistanceKmOffsetAdjuster(1.94),
            DepotManualProofOfDeliveryClaimAdjuster(
                mutableMapOf<String, Double>("south" to 1.36, "east" to 1.31),
            ),
            FlaggedManualProofOfDeliveryClaimAdjuster(flags, "manual-proof-of-delivery-claim-seasonal", 2.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 23396.8,
    )
}
