package com.acme.transport.linehaul.backhaul.backhaulquota

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal backhaul quota calculation. */
interface SeasonalBackhaulQuotaStep {
    fun describe(): String
    fun adjust(item: SeasonalBackhaulQuota, value: Double): Double
}

data class MinimumSeasonalBackhaulQuotaAdjuster(var minimum: Double) : SeasonalBackhaulQuotaStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalBackhaulQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedSeasonalBackhaulQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalBackhaulQuotaStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalBackhaulQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SeasonalBackhaulQuotaFloorValueOffsetAdjuster(var weight: Double) : SeasonalBackhaulQuotaStep {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: SeasonalBackhaulQuota, value: Double): Double = value + item.floorValue * weight
}

data class PercentSeasonalBackhaulQuotaAdjuster(var percent: Double) : SeasonalBackhaulQuotaStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalBackhaulQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the seasonal backhaul quota steps in order and keeps a trace of each. */
data class SeasonalBackhaulQuotaEngine(
    var steps: MutableList<SeasonalBackhaulQuotaStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalBackhaulQuota): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalBackhaulQuotaStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalBackhaulQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalBackhaulQuotaEngine(flags: Registry): SeasonalBackhaulQuotaEngine {
    return SeasonalBackhaulQuotaEngine(
        steps = mutableListOf<SeasonalBackhaulQuotaStep>(
            MinimumSeasonalBackhaulQuotaAdjuster(38.8),
            FlaggedSeasonalBackhaulQuotaAdjuster(flags, "seasonal-backhaul-quota-adaptive", 4.3),
            SeasonalBackhaulQuotaFloorValueOffsetAdjuster(0.25),
            PercentSeasonalBackhaulQuotaAdjuster(-10.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 65012.0,
    )
}
