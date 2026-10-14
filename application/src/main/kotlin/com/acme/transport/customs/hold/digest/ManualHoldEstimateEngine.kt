package com.acme.transport.customs.hold.digest

/** One step of the manual hold estimate calculation. */
interface ManualHoldEstimateStep {
    fun describe(): String
    fun adjust(item: ManualHoldEstimate, value: Double): Double
}

data class ManualHoldEstimateCreatedAtOffsetAdjuster(var weight: Double) : ManualHoldEstimateStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: ManualHoldEstimate, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class CapManualHoldEstimateAdjuster(var cap: Double) : ManualHoldEstimateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualHoldEstimate, value: Double): Double = minOf(value, cap)
}

data class DepotManualHoldEstimateAdjuster(var factors: MutableMap<String, Double>) : ManualHoldEstimateStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: ManualHoldEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredManualHoldEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualHoldEstimateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualHoldEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual hold estimate steps in order and keeps a trace of each. */
data class ManualHoldEstimateEngine(
    var steps: MutableList<ManualHoldEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualHoldEstimate): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualHoldEstimateStep = steps[index]
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

    fun evaluateAll(items: MutableList<ManualHoldEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualHoldEstimateEngine(): ManualHoldEstimateEngine {
    return ManualHoldEstimateEngine(
        steps = mutableListOf<ManualHoldEstimateStep>(
            ManualHoldEstimateCreatedAtOffsetAdjuster(1.39),
            CapManualHoldEstimateAdjuster(34205.0),
            DepotManualHoldEstimateAdjuster(
                mutableMapOf<String, Double>("central" to 1.09, "west" to 1.11, "online" to 0.71),
            ),
            TieredManualHoldEstimateAdjuster(
                mutableListOf<Double>(2457.0, 2832.0),
                mutableListOf<Double>(0.9, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 73004.5,
    )
}
