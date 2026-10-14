package com.acme.transport.fleetops.inspection.estimate

/** One step of the fallback inspection draft calculation. */
interface FallbackInspectionDraftModifier {
    fun describe(): String
    fun adjust(item: FallbackInspectionDraft, value: Double): Double
}

data class FallbackInspectionDraftEtaMinutesOffsetAdjuster(
    var weight: Double,
) : FallbackInspectionDraftModifier {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: FallbackInspectionDraft, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class TieredFallbackInspectionDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackInspectionDraftModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackInspectionDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentFallbackInspectionDraftAdjuster(var percent: Double) : FallbackInspectionDraftModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackInspectionDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFallbackInspectionDraftAdjuster(var cap: Double) : FallbackInspectionDraftModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackInspectionDraft, value: Double): Double = minOf(value, cap)
}

data class CountryFallbackInspectionDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackInspectionDraftModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: FallbackInspectionDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fallback inspection draft steps in order and keeps a trace of each. */
data class FallbackInspectionDraftEngine(
    var steps: MutableList<FallbackInspectionDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackInspectionDraft): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackInspectionDraftModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackInspectionDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackInspectionDraftEngine(): FallbackInspectionDraftEngine {
    return FallbackInspectionDraftEngine(
        steps = mutableListOf<FallbackInspectionDraftModifier>(
            FallbackInspectionDraftEtaMinutesOffsetAdjuster(1.69),
            TieredFallbackInspectionDraftAdjuster(
                mutableListOf<Double>(1324.0, 3335.0, 5541.0),
                mutableListOf<Double>(1.17, 1.07, 0.93),
            ),
            PercentFallbackInspectionDraftAdjuster(-4.1),
            CapFallbackInspectionDraftAdjuster(31459.0),
            CountryFallbackInspectionDraftAdjuster(
                mutableMapOf<String, Double>("retail" to 1.27, "online" to 1.32, "south" to 1.23),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 4683.6,
    )
}
