package com.acme.transport.emissions.fuelburn.burnrequest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal fuel burn group calculation. */
interface SeasonalFuelBurnGroupModifier {
    fun describe(): String
    fun adjust(item: SeasonalFuelBurnGroup, value: Double): Double
}

data class FlaggedSeasonalFuelBurnGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalFuelBurnGroupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalFuelBurnGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SeasonalFuelBurnGroupWeightOffsetAdjuster(var weight: Double) : SeasonalFuelBurnGroupModifier {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: SeasonalFuelBurnGroup, value: Double): Double = value + item.weight * weight
}

data class PercentSeasonalFuelBurnGroupAdjuster(var percent: Double) : SeasonalFuelBurnGroupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalFuelBurnGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameSeasonalFuelBurnGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalFuelBurnGroupModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: SeasonalFuelBurnGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingSeasonalFuelBurnGroupAdjuster(var step: Double) : SeasonalFuelBurnGroupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalFuelBurnGroup, value: Double): Double = roundTo(value, step)
}

data class CapSeasonalFuelBurnGroupAdjuster(var cap: Double) : SeasonalFuelBurnGroupModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalFuelBurnGroup, value: Double): Double = minOf(value, cap)
}

/** Runs the seasonal fuel burn group steps in order and keeps a trace of each. */
data class SeasonalFuelBurnGroupEngine(
    var steps: MutableList<SeasonalFuelBurnGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalFuelBurnGroup): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalFuelBurnGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalFuelBurnGroupEngine(flags: Registry): SeasonalFuelBurnGroupEngine {
    return SeasonalFuelBurnGroupEngine(
        steps = mutableListOf<SeasonalFuelBurnGroupModifier>(
            FlaggedSeasonalFuelBurnGroupAdjuster(flags, "seasonal-fuel-burn-group-tiered", 3.4),
            SeasonalFuelBurnGroupWeightOffsetAdjuster(0.14),
            PercentSeasonalFuelBurnGroupAdjuster(16.1),
            NameSeasonalFuelBurnGroupAdjuster(
                mutableMapOf<String, Double>("retail" to 0.83, "west" to 1.34, "online" to 0.81),
            ),
            RoundingSeasonalFuelBurnGroupAdjuster(1.0),
            CapSeasonalFuelBurnGroupAdjuster(22424.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 38879.2,
    )
}
