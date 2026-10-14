package com.acme.transport.crewing.qualification.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal qualification notice calculation. */
interface SeasonalQualificationNoticeModifier {
    fun describe(): String
    fun adjust(item: SeasonalQualificationNotice, value: Double): Double
}

data class PercentSeasonalQualificationNoticeAdjuster(
    var percent: Double,
) : SeasonalQualificationNoticeModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalQualificationNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SeasonalQualificationNoticeAmountOffsetAdjuster(
    var weight: Double,
) : SeasonalQualificationNoticeModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: SeasonalQualificationNotice, value: Double): Double = value + item.amount * weight
}

data class RoundingSeasonalQualificationNoticeAdjuster(var step: Double) : SeasonalQualificationNoticeModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalQualificationNotice, value: Double): Double = roundTo(value, step)
}

data class FlaggedSeasonalQualificationNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalQualificationNoticeModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalQualificationNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal qualification notice steps in order and keeps a trace of each. */
data class SeasonalQualificationNoticeEngine(
    var steps: MutableList<SeasonalQualificationNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalQualificationNotice): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalQualificationNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalQualificationNoticeEngine(flags: Registry): SeasonalQualificationNoticeEngine {
    return SeasonalQualificationNoticeEngine(
        steps = mutableListOf<SeasonalQualificationNoticeModifier>(
            PercentSeasonalQualificationNoticeAdjuster(2.2),
            SeasonalQualificationNoticeAmountOffsetAdjuster(0.42),
            RoundingSeasonalQualificationNoticeAdjuster(0.05),
            FlaggedSeasonalQualificationNoticeAdjuster(flags, "seasonal-qualification-notice-weighted", -8.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 4963.5,
    )
}
