package com.acme.transport.network.zone.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy zone tier calculation. */
interface LegacyZoneTierFactor {
    fun describe(): String
    fun adjust(item: LegacyZoneTier, value: Double): Double
}

data class RoundingLegacyZoneTierAdjuster(var step: Double) : LegacyZoneTierFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyZoneTier, value: Double): Double = roundTo(value, step)
}

data class FlaggedLegacyZoneTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyZoneTierFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyZoneTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLegacyZoneTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyZoneTierFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyZoneTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLegacyZoneTierAdjuster(var percent: Double) : LegacyZoneTierFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyZoneTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy zone tier steps in order and keeps a trace of each. */
data class LegacyZoneTierEngine(
    var steps: MutableList<LegacyZoneTierFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyZoneTier): Double {
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

    fun evaluateAll(items: MutableList<LegacyZoneTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyZoneTierEngine(flags: Registry): LegacyZoneTierEngine {
    return LegacyZoneTierEngine(
        steps = mutableListOf<LegacyZoneTierFactor>(
            RoundingLegacyZoneTierAdjuster(1.0),
            FlaggedLegacyZoneTierAdjuster(flags, "legacy-zone-tier-rolling", -7.1),
            TieredLegacyZoneTierAdjuster(
                mutableListOf<Double>(960.0, 2560.0, 4271.0),
                mutableListOf<Double>(0.94, 1.2, 1.12),
            ),
            PercentLegacyZoneTierAdjuster(10.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 31131.2,
    )
}
