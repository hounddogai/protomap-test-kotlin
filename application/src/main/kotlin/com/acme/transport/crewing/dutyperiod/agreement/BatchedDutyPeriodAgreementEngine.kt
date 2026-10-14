package com.acme.transport.crewing.dutyperiod.agreement

/** One step of the batched duty period agreement calculation. */
interface BatchedDutyPeriodAgreementAdjuster {
    fun describe(): String
    fun adjust(item: BatchedDutyPeriodAgreement, value: Double): Double
}

data class TieredBatchedDutyPeriodAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedDutyPeriodAgreementAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedDutyPeriodAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentBatchedDutyPeriodAgreementAdjuster(var percent: Double) : BatchedDutyPeriodAgreementAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedDutyPeriodAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class BatchedDutyPeriodAgreementBudgetOffsetAdjuster(
    var weight: Double,
) : BatchedDutyPeriodAgreementAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: BatchedDutyPeriodAgreement, value: Double): Double = value + item.budget * weight
}

/** Runs the batched duty period agreement steps in order and keeps a trace of each. */
data class BatchedDutyPeriodAgreementEngine(
    var steps: MutableList<BatchedDutyPeriodAgreementAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedDutyPeriodAgreement): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedDutyPeriodAgreementAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedDutyPeriodAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedDutyPeriodAgreementEngine(): BatchedDutyPeriodAgreementEngine {
    return BatchedDutyPeriodAgreementEngine(
        steps = mutableListOf<BatchedDutyPeriodAgreementAdjuster>(
            TieredBatchedDutyPeriodAgreementAdjuster(
                mutableListOf<Double>(1159.0, 7448.0, 7804.0),
                mutableListOf<Double>(0.84, 0.88, 0.86),
            ),
            PercentBatchedDutyPeriodAgreementAdjuster(13.8),
            BatchedDutyPeriodAgreementBudgetOffsetAdjuster(1.52),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 43820.9,
    )
}
