package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual proof of delivery mapping calculation. */
interface ManualProofOfDeliveryMappingFactor {
    fun describe(): String
    fun adjust(item: ManualProofOfDeliveryMapping, value: Double): Double
}

data class CapManualProofOfDeliveryMappingAdjuster(var cap: Double) : ManualProofOfDeliveryMappingFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualProofOfDeliveryMapping, value: Double): Double = minOf(value, cap)
}

data class ManualProofOfDeliveryMappingQuantityOffsetAdjuster(
    var weight: Double,
) : ManualProofOfDeliveryMappingFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: ManualProofOfDeliveryMapping, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class FlaggedManualProofOfDeliveryMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualProofOfDeliveryMappingFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualProofOfDeliveryMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingManualProofOfDeliveryMappingAdjuster(var step: Double) : ManualProofOfDeliveryMappingFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualProofOfDeliveryMapping, value: Double): Double = roundTo(value, step)
}

data class DepotManualProofOfDeliveryMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualProofOfDeliveryMappingFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: ManualProofOfDeliveryMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual proof of delivery mapping steps in order and keeps a trace of each. */
data class ManualProofOfDeliveryMappingEngine(
    var steps: MutableList<ManualProofOfDeliveryMappingFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualProofOfDeliveryMapping): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualProofOfDeliveryMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualProofOfDeliveryMappingEngine(flags: Registry): ManualProofOfDeliveryMappingEngine {
    return ManualProofOfDeliveryMappingEngine(
        steps = mutableListOf<ManualProofOfDeliveryMappingFactor>(
            CapManualProofOfDeliveryMappingAdjuster(23710.0),
            ManualProofOfDeliveryMappingQuantityOffsetAdjuster(0.32),
            FlaggedManualProofOfDeliveryMappingAdjuster(
                flags,
                "manual-proof-of-delivery-mapping-regional",
                -3.0,
            ),
            RoundingManualProofOfDeliveryMappingAdjuster(1.0),
            DepotManualProofOfDeliveryMappingAdjuster(
                mutableMapOf<String, Double>("west" to 0.93, "east" to 1.08, "central" to 0.96),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 28324.8,
    )
}
