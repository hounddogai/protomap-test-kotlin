package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual proof of delivery adjustment calculation. */
interface ManualProofOfDeliveryAdjustmentStep {
    fun describe(): String
    fun adjust(item: ManualProofOfDeliveryAdjustment, value: Double): Double
}

data class TieredManualProofOfDeliveryAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualProofOfDeliveryAdjustmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualProofOfDeliveryAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentManualProofOfDeliveryAdjustmentAdjuster(
    var percent: Double,
) : ManualProofOfDeliveryAdjustmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualProofOfDeliveryAdjustment, value: Double): Double {
        return value * (1.0 + percent / 100.0)
    }
}

data class ManualProofOfDeliveryAdjustmentFloorValueOffsetAdjuster(
    var weight: Double,
) : ManualProofOfDeliveryAdjustmentStep {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: ManualProofOfDeliveryAdjustment, value: Double): Double {
        return value + item.floorValue * weight
    }
}

data class CapManualProofOfDeliveryAdjustmentAdjuster(var cap: Double) : ManualProofOfDeliveryAdjustmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualProofOfDeliveryAdjustment, value: Double): Double = minOf(value, cap)
}

data class SourceManualProofOfDeliveryAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualProofOfDeliveryAdjustmentStep {
    override fun describe(): String = "by source"

    override fun adjust(item: ManualProofOfDeliveryAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual proof of delivery adjustment steps in order and keeps a trace of each. */
data class ManualProofOfDeliveryAdjustmentEngine(
    var steps: MutableList<ManualProofOfDeliveryAdjustmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualProofOfDeliveryAdjustment): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualProofOfDeliveryAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualProofOfDeliveryAdjustmentEngine(): ManualProofOfDeliveryAdjustmentEngine {
    return ManualProofOfDeliveryAdjustmentEngine(
        steps = mutableListOf<ManualProofOfDeliveryAdjustmentStep>(
            TieredManualProofOfDeliveryAdjustmentAdjuster(
                mutableListOf<Double>(3865.0, 7440.0, 8782.0),
                mutableListOf<Double>(1.12, 0.96, 1.18),
            ),
            PercentManualProofOfDeliveryAdjustmentAdjuster(-9.5),
            ManualProofOfDeliveryAdjustmentFloorValueOffsetAdjuster(0.6),
            CapManualProofOfDeliveryAdjustmentAdjuster(21515.0),
            SourceManualProofOfDeliveryAdjustmentAdjuster(
                mutableMapOf<String, Double>("online" to 0.8, "retail" to 1.07, "central" to 0.74),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 44275.6,
    )
}
