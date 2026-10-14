package com.acme.transport.linehaul.trailer.regionaltrailerrecord

/** One step of the manual trailer target calculation. */
interface ManualTrailerTargetFactor {
    fun describe(): String
    fun adjust(item: ManualTrailerTarget, value: Double): Double
}

data class CapManualTrailerTargetAdjuster(var cap: Double) : ManualTrailerTargetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualTrailerTarget, value: Double): Double = minOf(value, cap)
}

data class LocaleManualTrailerTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualTrailerTargetFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: ManualTrailerTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentManualTrailerTargetAdjuster(var percent: Double) : ManualTrailerTargetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualTrailerTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ManualTrailerTargetBudgetOffsetAdjuster(var weight: Double) : ManualTrailerTargetFactor {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: ManualTrailerTarget, value: Double): Double = value + item.budget * weight
}

data class TieredManualTrailerTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualTrailerTargetFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualTrailerTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual trailer target steps in order and keeps a trace of each. */
data class ManualTrailerTargetEngine(
    var steps: MutableList<ManualTrailerTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualTrailerTarget): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualTrailerTargetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ManualTrailerTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualTrailerTargetEngine(): ManualTrailerTargetEngine {
    return ManualTrailerTargetEngine(
        steps = mutableListOf<ManualTrailerTargetFactor>(
            CapManualTrailerTargetAdjuster(24551.0),
            LocaleManualTrailerTargetAdjuster(
                mutableMapOf<String, Double>("west" to 0.89, "north" to 1.2, "east" to 0.89),
            ),
            PercentManualTrailerTargetAdjuster(-0.0),
            ManualTrailerTargetBudgetOffsetAdjuster(0.08),
            TieredManualTrailerTargetAdjuster(
                mutableListOf<Double>(1106.0, 3438.0, 6474.0, 9675.0),
                mutableListOf<Double>(1.08, 1.14, 1.12, 0.85),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 38736.6,
    )
}
