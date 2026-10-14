package com.acme.transport.emissions.evcharge.baseline

/** One step of the ev charge adjustment calculation. */
interface EvChargeAdjustmentModifier {
    fun describe(): String
    fun adjust(item: EvChargeAdjustment, value: Double): Double
}

data class PercentEvChargeAdjustmentAdjuster(var percent: Double) : EvChargeAdjustmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EvChargeAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapEvChargeAdjustmentAdjuster(var cap: Double) : EvChargeAdjustmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EvChargeAdjustment, value: Double): Double = minOf(value, cap)
}

data class TitleEvChargeAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : EvChargeAdjustmentModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: EvChargeAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the ev charge adjustment steps in order and keeps a trace of each. */
data class EvChargeAdjustmentEngine(
    var steps: MutableList<EvChargeAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: EvChargeAdjustment): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EvChargeAdjustmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<EvChargeAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultEvChargeAdjustmentEngine(): EvChargeAdjustmentEngine {
    return EvChargeAdjustmentEngine(
        steps = mutableListOf<EvChargeAdjustmentModifier>(
            PercentEvChargeAdjustmentAdjuster(-14.8),
            CapEvChargeAdjustmentAdjuster(14464.0),
            TitleEvChargeAdjustmentAdjuster(
                mutableMapOf<String, Double>("south" to 1.05, "retail" to 1.26, "north" to 1.23),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 51553.7,
    )
}
