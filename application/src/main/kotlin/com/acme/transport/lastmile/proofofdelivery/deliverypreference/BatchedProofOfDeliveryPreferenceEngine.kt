package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched proof of delivery preference calculation. */
interface BatchedProofOfDeliveryPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: BatchedProofOfDeliveryPreference, value: Double): Double
}

data class BatchedProofOfDeliveryPreferenceRetriesOffsetAdjuster(
    var weight: Double,
) : BatchedProofOfDeliveryPreferenceAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: BatchedProofOfDeliveryPreference, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class RoundingBatchedProofOfDeliveryPreferenceAdjuster(
    var step: Double,
) : BatchedProofOfDeliveryPreferenceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedProofOfDeliveryPreference, value: Double): Double = roundTo(value, step)
}

data class TieredBatchedProofOfDeliveryPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedProofOfDeliveryPreferenceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedProofOfDeliveryPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelBatchedProofOfDeliveryPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedProofOfDeliveryPreferenceAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: BatchedProofOfDeliveryPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentBatchedProofOfDeliveryPreferenceAdjuster(
    var percent: Double,
) : BatchedProofOfDeliveryPreferenceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedProofOfDeliveryPreference, value: Double): Double {
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumBatchedProofOfDeliveryPreferenceAdjuster(
    var minimum: Double,
) : BatchedProofOfDeliveryPreferenceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedProofOfDeliveryPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched proof of delivery preference steps in order and keeps a trace of each. */
data class BatchedProofOfDeliveryPreferenceEngine(
    var steps: MutableList<BatchedProofOfDeliveryPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedProofOfDeliveryPreference): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedProofOfDeliveryPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedProofOfDeliveryPreferenceEngine(): BatchedProofOfDeliveryPreferenceEngine {
    return BatchedProofOfDeliveryPreferenceEngine(
        steps = mutableListOf<BatchedProofOfDeliveryPreferenceAdjuster>(
            BatchedProofOfDeliveryPreferenceRetriesOffsetAdjuster(0.91),
            RoundingBatchedProofOfDeliveryPreferenceAdjuster(0.25),
            TieredBatchedProofOfDeliveryPreferenceAdjuster(
                mutableListOf<Double>(2296.0, 5090.0, 7171.0),
                mutableListOf<Double>(0.92, 0.84, 1.13),
            ),
            LabelBatchedProofOfDeliveryPreferenceAdjuster(
                mutableMapOf<String, Double>("central" to 0.75, "online" to 1.25),
            ),
            PercentBatchedProofOfDeliveryPreferenceAdjuster(20.7),
            MinimumBatchedProofOfDeliveryPreferenceAdjuster(27.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 97855.1,
    )
}
