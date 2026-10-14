package com.acme.transport.fleetops.defect.defectsummary

/** One step of the seasonal defect notice calculation. */
interface SeasonalDefectNoticeFactor {
    fun describe(): String
    fun adjust(item: SeasonalDefectNotice, value: Double): Double
}

data class NameSeasonalDefectNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalDefectNoticeFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: SeasonalDefectNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class SeasonalDefectNoticeAttemptsOffsetAdjuster(var weight: Double) : SeasonalDefectNoticeFactor {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: SeasonalDefectNotice, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class PercentSeasonalDefectNoticeAdjuster(var percent: Double) : SeasonalDefectNoticeFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalDefectNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the seasonal defect notice steps in order and keeps a trace of each. */
data class SeasonalDefectNoticeEngine(
    var steps: MutableList<SeasonalDefectNoticeFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalDefectNotice): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalDefectNoticeFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalDefectNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalDefectNoticeEngine(): SeasonalDefectNoticeEngine {
    return SeasonalDefectNoticeEngine(
        steps = mutableListOf<SeasonalDefectNoticeFactor>(
            NameSeasonalDefectNoticeAdjuster(mutableMapOf<String, Double>("central" to 1.3)),
            SeasonalDefectNoticeAttemptsOffsetAdjuster(0.56),
            PercentSeasonalDefectNoticeAdjuster(-8.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 56221.5,
    )
}
