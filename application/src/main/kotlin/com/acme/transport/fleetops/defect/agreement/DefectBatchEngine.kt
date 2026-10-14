package com.acme.transport.fleetops.defect.agreement

/** One step of the defect batch calculation. */
interface DefectBatchFactor {
    fun describe(): String
    fun adjust(item: DefectBatch, value: Double): Double
}

data class PercentDefectBatchAdjuster(var percent: Double) : DefectBatchFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefectBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CountryDefectBatchAdjuster(var factors: MutableMap<String, Double>) : DefectBatchFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: DefectBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDefectBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefectBatchFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefectBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the defect batch steps in order and keeps a trace of each. */
data class DefectBatchEngine(
    var steps: MutableList<DefectBatchFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefectBatch): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefectBatchFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DefectBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefectBatchEngine(): DefectBatchEngine {
    return DefectBatchEngine(
        steps = mutableListOf<DefectBatchFactor>(
            PercentDefectBatchAdjuster(22.1),
            CountryDefectBatchAdjuster(mutableMapOf<String, Double>("online" to 0.99, "retail" to 1.27)),
            TieredDefectBatchAdjuster(
                mutableListOf<Double>(618.0, 2673.0, 4589.0),
                mutableListOf<Double>(1.11, 1.1, 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 3713.6,
    )
}
