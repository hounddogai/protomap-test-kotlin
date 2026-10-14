package com.acme.transport.freightpay.dispute.disputepolicy

/** One step of the seasonal dispute tier calculation. */
interface SeasonalDisputeTierModifier {
    fun describe(): String
    fun adjust(item: SeasonalDisputeTier, value: Double): Double
}

data class PercentSeasonalDisputeTierAdjuster(var percent: Double) : SeasonalDisputeTierModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalDisputeTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapSeasonalDisputeTierAdjuster(var cap: Double) : SeasonalDisputeTierModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalDisputeTier, value: Double): Double = minOf(value, cap)
}

data class RegionSeasonalDisputeTierAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalDisputeTierModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: SeasonalDisputeTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the seasonal dispute tier steps in order and keeps a trace of each. */
data class SeasonalDisputeTierEngine(
    var steps: MutableList<SeasonalDisputeTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalDisputeTier): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalDisputeTierModifier = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalDisputeTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalDisputeTierEngine(): SeasonalDisputeTierEngine {
    return SeasonalDisputeTierEngine(
        steps = mutableListOf<SeasonalDisputeTierModifier>(
            PercentSeasonalDisputeTierAdjuster(11.9),
            CapSeasonalDisputeTierAdjuster(25468.0),
            RegionSeasonalDisputeTierAdjuster(mutableMapOf<String, Double>("central" to 1.14, "west" to 0.77)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 46314.3,
    )
}
