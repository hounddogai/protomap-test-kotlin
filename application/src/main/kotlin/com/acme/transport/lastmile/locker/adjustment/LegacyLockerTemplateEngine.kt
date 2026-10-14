package com.acme.transport.lastmile.locker.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy locker template calculation. */
interface LegacyLockerTemplateAdjuster {
    fun describe(): String
    fun adjust(item: LegacyLockerTemplate, value: Double): Double
}

data class FlaggedLegacyLockerTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyLockerTemplateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyLockerTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLegacyLockerTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyLockerTemplateAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyLockerTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LegacyLockerTemplateWindowDaysOffsetAdjuster(var weight: Double) : LegacyLockerTemplateAdjuster {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: LegacyLockerTemplate, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

/** Runs the legacy locker template steps in order and keeps a trace of each. */
data class LegacyLockerTemplateEngine(
    var steps: MutableList<LegacyLockerTemplateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyLockerTemplate): Double {
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

    fun evaluateAll(items: MutableList<LegacyLockerTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyLockerTemplateEngine(flags: Registry): LegacyLockerTemplateEngine {
    return LegacyLockerTemplateEngine(
        steps = mutableListOf<LegacyLockerTemplateAdjuster>(
            FlaggedLegacyLockerTemplateAdjuster(flags, "legacy-locker-template-global", 1.9),
            TieredLegacyLockerTemplateAdjuster(
                mutableListOf<Double>(1935.0, 2465.0, 2741.0),
                mutableListOf<Double>(0.96, 1.01, 0.8),
            ),
            LegacyLockerTemplateWindowDaysOffsetAdjuster(1.44),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 2656.6,
    )
}
