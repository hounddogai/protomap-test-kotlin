package com.acme.transport.lastmile.proofofdelivery.exception

/** One step of the batched proof of delivery notice calculation. */
interface BatchedProofOfDeliveryNoticeFactor {
    fun describe(): String
    fun adjust(item: BatchedProofOfDeliveryNotice, value: Double): Double
}

data class TieredBatchedProofOfDeliveryNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedProofOfDeliveryNoticeFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedProofOfDeliveryNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumBatchedProofOfDeliveryNoticeAdjuster(
    var minimum: Double,
) : BatchedProofOfDeliveryNoticeFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedProofOfDeliveryNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class BatchedProofOfDeliveryNoticeRevisionOffsetAdjuster(
    var weight: Double,
) : BatchedProofOfDeliveryNoticeFactor {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: BatchedProofOfDeliveryNotice, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

/** Runs the batched proof of delivery notice steps in order and keeps a trace of each. */
data class BatchedProofOfDeliveryNoticeEngine(
    var steps: MutableList<BatchedProofOfDeliveryNoticeFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedProofOfDeliveryNotice): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedProofOfDeliveryNoticeFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedProofOfDeliveryNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedProofOfDeliveryNoticeEngine(): BatchedProofOfDeliveryNoticeEngine {
    return BatchedProofOfDeliveryNoticeEngine(
        steps = mutableListOf<BatchedProofOfDeliveryNoticeFactor>(
            TieredBatchedProofOfDeliveryNoticeAdjuster(
                mutableListOf<Double>(3702.0, 7208.0),
                mutableListOf<Double>(0.85, 1.17),
            ),
            MinimumBatchedProofOfDeliveryNoticeAdjuster(46.7),
            BatchedProofOfDeliveryNoticeRevisionOffsetAdjuster(1.59),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 90682.7,
    )
}
