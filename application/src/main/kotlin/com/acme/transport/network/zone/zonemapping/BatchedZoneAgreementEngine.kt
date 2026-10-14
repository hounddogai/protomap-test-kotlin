package com.acme.transport.network.zone.zonemapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched zone agreement calculation. */
interface BatchedZoneAgreementAdjuster {
    fun describe(): String
    fun adjust(item: BatchedZoneAgreement, value: Double): Double
}

data class RoundingBatchedZoneAgreementAdjuster(var step: Double) : BatchedZoneAgreementAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedZoneAgreement, value: Double): Double = roundTo(value, step)
}

data class BatchedZoneAgreementLimitOffsetAdjuster(var weight: Double) : BatchedZoneAgreementAdjuster {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: BatchedZoneAgreement, value: Double): Double = value + item.limit.toDouble() * weight
}

data class SourceBatchedZoneAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedZoneAgreementAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: BatchedZoneAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedBatchedZoneAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedZoneAgreementAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedZoneAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentBatchedZoneAgreementAdjuster(var percent: Double) : BatchedZoneAgreementAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedZoneAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched zone agreement steps in order and keeps a trace of each. */
data class BatchedZoneAgreementEngine(
    var steps: MutableList<BatchedZoneAgreementAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedZoneAgreement): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedZoneAgreementAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedZoneAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedZoneAgreementEngine(flags: Registry): BatchedZoneAgreementEngine {
    return BatchedZoneAgreementEngine(
        steps = mutableListOf<BatchedZoneAgreementAdjuster>(
            RoundingBatchedZoneAgreementAdjuster(0.25),
            BatchedZoneAgreementLimitOffsetAdjuster(0.63),
            SourceBatchedZoneAgreementAdjuster(mutableMapOf<String, Double>("west" to 1.19)),
            FlaggedBatchedZoneAgreementAdjuster(flags, "batched-zone-agreement-primary", -1.6),
            PercentBatchedZoneAgreementAdjuster(5.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 18125.4,
    )
}
