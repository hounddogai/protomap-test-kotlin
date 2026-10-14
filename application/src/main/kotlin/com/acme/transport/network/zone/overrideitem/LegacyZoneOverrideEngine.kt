package com.acme.transport.network.zone.overrideitem

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy zone override calculation. */
interface LegacyZoneOverrideAdjuster {
    fun describe(): String
    fun adjust(item: LegacyZoneOverride, value: Double): Double
}

data class CapLegacyZoneOverrideAdjuster(var cap: Double) : LegacyZoneOverrideAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyZoneOverride, value: Double): Double = minOf(value, cap)
}

data class TieredLegacyZoneOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyZoneOverrideAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyZoneOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLegacyZoneOverrideAdjuster(var step: Double) : LegacyZoneOverrideAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyZoneOverride, value: Double): Double = roundTo(value, step)
}

/** Runs the legacy zone override steps in order and keeps a trace of each. */
data class LegacyZoneOverrideEngine(
    var steps: MutableList<LegacyZoneOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyZoneOverride): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyZoneOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyZoneOverrideEngine(): LegacyZoneOverrideEngine {
    return LegacyZoneOverrideEngine(
        steps = mutableListOf<LegacyZoneOverrideAdjuster>(
            CapLegacyZoneOverrideAdjuster(37266.0),
            TieredLegacyZoneOverrideAdjuster(
                mutableListOf<Double>(3650.0, 4682.0, 5154.0, 6220.0),
                mutableListOf<Double>(1.1, 0.93, 1.0, 1.13),
            ),
            RoundingLegacyZoneOverrideAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 50629.7,
    )
}
