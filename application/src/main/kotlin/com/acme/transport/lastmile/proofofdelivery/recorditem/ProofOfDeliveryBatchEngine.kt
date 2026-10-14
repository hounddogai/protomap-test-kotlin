package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the proof of delivery batch calculation. */
interface ProofOfDeliveryBatchStep {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryBatch, value: Double): Double
}

data class FlaggedProofOfDeliveryBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ProofOfDeliveryBatchStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ProofOfDeliveryBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentProofOfDeliveryBatchAdjuster(var percent: Double) : ProofOfDeliveryBatchStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ProofOfDeliveryBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ChannelProofOfDeliveryBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : ProofOfDeliveryBatchStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: ProofOfDeliveryBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredProofOfDeliveryBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ProofOfDeliveryBatchStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ProofOfDeliveryBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingProofOfDeliveryBatchAdjuster(var step: Double) : ProofOfDeliveryBatchStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ProofOfDeliveryBatch, value: Double): Double = roundTo(value, step)
}

/** Runs the proof of delivery batch steps in order and keeps a trace of each. */
data class ProofOfDeliveryBatchEngine(
    var steps: MutableList<ProofOfDeliveryBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ProofOfDeliveryBatch): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ProofOfDeliveryBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryBatchEngine(flags: Registry): ProofOfDeliveryBatchEngine {
    return ProofOfDeliveryBatchEngine(
        steps = mutableListOf<ProofOfDeliveryBatchStep>(
            FlaggedProofOfDeliveryBatchAdjuster(flags, "proof-of-delivery-batch-pending", -7.2),
            PercentProofOfDeliveryBatchAdjuster(-20.6),
            ChannelProofOfDeliveryBatchAdjuster(mutableMapOf<String, Double>("north" to 1.24, "south" to 1.34)),
            TieredProofOfDeliveryBatchAdjuster(
                mutableListOf<Double>(4004.0, 5954.0),
                mutableListOf<Double>(1.04, 1.05),
            ),
            RoundingProofOfDeliveryBatchAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 73654.7,
    )
}
