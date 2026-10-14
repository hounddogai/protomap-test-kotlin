package com.acme.transport.terminal.traileryard.target

/** One step of the legacy trailer yard agreement calculation. */
interface LegacyTrailerYardAgreementFactor {
    fun describe(): String
    fun adjust(item: LegacyTrailerYardAgreement, value: Double): Double
}

data class CapLegacyTrailerYardAgreementAdjuster(var cap: Double) : LegacyTrailerYardAgreementFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyTrailerYardAgreement, value: Double): Double = minOf(value, cap)
}

data class DepotLegacyTrailerYardAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyTrailerYardAgreementFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: LegacyTrailerYardAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLegacyTrailerYardAgreementAdjuster(var minimum: Double) : LegacyTrailerYardAgreementFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyTrailerYardAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLegacyTrailerYardAgreementAdjuster(var percent: Double) : LegacyTrailerYardAgreementFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyTrailerYardAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLegacyTrailerYardAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyTrailerYardAgreementFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyTrailerYardAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy trailer yard agreement steps in order and keeps a trace of each. */
data class LegacyTrailerYardAgreementEngine(
    var steps: MutableList<LegacyTrailerYardAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyTrailerYardAgreement): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyTrailerYardAgreementFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyTrailerYardAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyTrailerYardAgreementEngine(): LegacyTrailerYardAgreementEngine {
    return LegacyTrailerYardAgreementEngine(
        steps = mutableListOf<LegacyTrailerYardAgreementFactor>(
            CapLegacyTrailerYardAgreementAdjuster(19769.0),
            DepotLegacyTrailerYardAgreementAdjuster(
                mutableMapOf<String, Double>("south" to 1.21, "east" to 1.33),
            ),
            MinimumLegacyTrailerYardAgreementAdjuster(15.7),
            PercentLegacyTrailerYardAgreementAdjuster(-16.8),
            TieredLegacyTrailerYardAgreementAdjuster(
                mutableListOf<Double>(1352.0, 6417.0, 7294.0),
                mutableListOf<Double>(0.91, 0.89, 0.88),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 96068.7,
    )
}
