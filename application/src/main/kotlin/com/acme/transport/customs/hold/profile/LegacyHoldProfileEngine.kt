package com.acme.transport.customs.hold.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy hold profile calculation. */
interface LegacyHoldProfileStep {
    fun describe(): String
    fun adjust(item: LegacyHoldProfile, value: Double): Double
}

data class PercentLegacyHoldProfileAdjuster(var percent: Double) : LegacyHoldProfileStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyHoldProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLegacyHoldProfileAdjuster(var cap: Double) : LegacyHoldProfileStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyHoldProfile, value: Double): Double = minOf(value, cap)
}

data class TieredLegacyHoldProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyHoldProfileStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyHoldProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedLegacyHoldProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyHoldProfileStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyHoldProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LegacyHoldProfileVersionOffsetAdjuster(var weight: Double) : LegacyHoldProfileStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: LegacyHoldProfile, value: Double): Double = value + item.version.toDouble() * weight
}

data class UnitLegacyHoldProfileAdjuster(var factors: MutableMap<String, Double>) : LegacyHoldProfileStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: LegacyHoldProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the legacy hold profile steps in order and keeps a trace of each. */
data class LegacyHoldProfileEngine(
    var steps: MutableList<LegacyHoldProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyHoldProfile): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyHoldProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyHoldProfileEngine(flags: Registry): LegacyHoldProfileEngine {
    return LegacyHoldProfileEngine(
        steps = mutableListOf<LegacyHoldProfileStep>(
            PercentLegacyHoldProfileAdjuster(4.8),
            CapLegacyHoldProfileAdjuster(48749.0),
            TieredLegacyHoldProfileAdjuster(
                mutableListOf<Double>(2429.0, 3674.0),
                mutableListOf<Double>(1.2, 1.05),
            ),
            FlaggedLegacyHoldProfileAdjuster(flags, "legacy-hold-profile-lenient", 9.9),
            LegacyHoldProfileVersionOffsetAdjuster(0.66),
            UnitLegacyHoldProfileAdjuster(mutableMapOf<String, Double>("online" to 1.33, "north" to 1.12)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 47755.5,
    )
}
