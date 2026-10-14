package com.acme.transport.safety.scorecard.scorecardthreshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered scorecard cycle calculation. */
interface TieredScorecardCycleFactor {
    fun describe(): String
    fun adjust(item: TieredScorecardCycle, value: Double): Double
}

data class TieredScorecardCycleOdometerOffsetAdjuster(var weight: Double) : TieredScorecardCycleFactor {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: TieredScorecardCycle, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class FlaggedTieredScorecardCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredScorecardCycleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredScorecardCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTieredScorecardCycleAdjuster(var cap: Double) : TieredScorecardCycleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredScorecardCycle, value: Double): Double = minOf(value, cap)
}

data class LocaleTieredScorecardCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredScorecardCycleFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: TieredScorecardCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTieredScorecardCycleAdjuster(var step: Double) : TieredScorecardCycleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredScorecardCycle, value: Double): Double = roundTo(value, step)
}

/** Runs the tiered scorecard cycle steps in order and keeps a trace of each. */
data class TieredScorecardCycleEngine(
    var steps: MutableList<TieredScorecardCycleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredScorecardCycle): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredScorecardCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredScorecardCycleEngine(flags: Registry): TieredScorecardCycleEngine {
    return TieredScorecardCycleEngine(
        steps = mutableListOf<TieredScorecardCycleFactor>(
            TieredScorecardCycleOdometerOffsetAdjuster(1.86),
            FlaggedTieredScorecardCycleAdjuster(flags, "tiered-scorecard-cycle-scheduled", 0.5),
            CapTieredScorecardCycleAdjuster(34546.0),
            LocaleTieredScorecardCycleAdjuster(
                mutableMapOf<String, Double>("retail" to 0.93, "west" to 0.9, "online" to 1.07),
            ),
            RoundingTieredScorecardCycleAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 50571.3,
    )
}
