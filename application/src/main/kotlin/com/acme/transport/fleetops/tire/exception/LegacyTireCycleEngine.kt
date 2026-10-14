package com.acme.transport.fleetops.tire.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy tire cycle calculation. */
interface LegacyTireCycleStep {
    fun describe(): String
    fun adjust(item: LegacyTireCycle, value: Double): Double
}

data class TieredLegacyTireCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyTireCycleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyTireCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleLegacyTireCycleAdjuster(var factors: MutableMap<String, Double>) : LegacyTireCycleStep {
    override fun describe(): String = "by title"

    override fun adjust(item: LegacyTireCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLegacyTireCycleAdjuster(var step: Double) : LegacyTireCycleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyTireCycle, value: Double): Double = roundTo(value, step)
}

data class FlaggedLegacyTireCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyTireCycleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyTireCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLegacyTireCycleAdjuster(var percent: Double) : LegacyTireCycleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyTireCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy tire cycle steps in order and keeps a trace of each. */
data class LegacyTireCycleEngine(
    var steps: MutableList<LegacyTireCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyTireCycle): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyTireCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyTireCycleEngine(flags: Registry): LegacyTireCycleEngine {
    return LegacyTireCycleEngine(
        steps = mutableListOf<LegacyTireCycleStep>(
            TieredLegacyTireCycleAdjuster(
                mutableListOf<Double>(1734.0, 2495.0, 6954.0),
                mutableListOf<Double>(1.05, 1.08, 0.8),
            ),
            TitleLegacyTireCycleAdjuster(mutableMapOf<String, Double>("east" to 1.12, "central" to 0.92)),
            RoundingLegacyTireCycleAdjuster(0.05),
            FlaggedLegacyTireCycleAdjuster(flags, "legacy-tire-cycle-lenient", -5.3),
            PercentLegacyTireCycleAdjuster(-21.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 87620.0,
    )
}
