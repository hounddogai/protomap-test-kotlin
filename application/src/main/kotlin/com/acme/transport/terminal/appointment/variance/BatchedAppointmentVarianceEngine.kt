package com.acme.transport.terminal.appointment.variance

/** One step of the batched appointment variance calculation. */
interface BatchedAppointmentVarianceStep {
    fun describe(): String
    fun adjust(item: BatchedAppointmentVariance, value: Double): Double
}

data class CategoryBatchedAppointmentVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedAppointmentVarianceStep {
    override fun describe(): String = "by category"

    override fun adjust(item: BatchedAppointmentVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredBatchedAppointmentVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedAppointmentVarianceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedAppointmentVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class BatchedAppointmentVarianceMarginOffsetAdjuster(var weight: Double) : BatchedAppointmentVarianceStep {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: BatchedAppointmentVariance, value: Double): Double = value + item.margin * weight
}

/** Runs the batched appointment variance steps in order and keeps a trace of each. */
data class BatchedAppointmentVarianceEngine(
    var steps: MutableList<BatchedAppointmentVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedAppointmentVariance): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedAppointmentVarianceStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedAppointmentVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedAppointmentVarianceEngine(): BatchedAppointmentVarianceEngine {
    return BatchedAppointmentVarianceEngine(
        steps = mutableListOf<BatchedAppointmentVarianceStep>(
            CategoryBatchedAppointmentVarianceAdjuster(
                mutableMapOf<String, Double>("retail" to 0.79, "central" to 0.78, "west" to 1.07),
            ),
            TieredBatchedAppointmentVarianceAdjuster(
                mutableListOf<Double>(9415.0, 9894.0),
                mutableListOf<Double>(1.1, 1.09),
            ),
            BatchedAppointmentVarianceMarginOffsetAdjuster(0.49),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 34621.8,
    )
}
