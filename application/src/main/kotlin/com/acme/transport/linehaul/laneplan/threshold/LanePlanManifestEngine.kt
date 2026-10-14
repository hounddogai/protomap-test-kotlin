package com.acme.transport.linehaul.laneplan.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lane plan manifest calculation. */
interface LanePlanManifestStep {
    fun describe(): String
    fun adjust(item: LanePlanManifest, value: Double): Double
}

data class RoundingLanePlanManifestAdjuster(var step: Double) : LanePlanManifestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LanePlanManifest, value: Double): Double = roundTo(value, step)
}

data class FlaggedLanePlanManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LanePlanManifestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LanePlanManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLanePlanManifestAdjuster(var cap: Double) : LanePlanManifestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LanePlanManifest, value: Double): Double = minOf(value, cap)
}

data class MinimumLanePlanManifestAdjuster(var minimum: Double) : LanePlanManifestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LanePlanManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredLanePlanManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LanePlanManifestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LanePlanManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lane plan manifest steps in order and keeps a trace of each. */
data class LanePlanManifestEngine(
    var steps: MutableList<LanePlanManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LanePlanManifest): Double {
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

    fun evaluateAll(items: MutableList<LanePlanManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLanePlanManifestEngine(flags: Registry): LanePlanManifestEngine {
    return LanePlanManifestEngine(
        steps = mutableListOf<LanePlanManifestStep>(
            RoundingLanePlanManifestAdjuster(5.0),
            FlaggedLanePlanManifestAdjuster(flags, "lane-plan-manifest-global", -2.9),
            CapLanePlanManifestAdjuster(21193.0),
            MinimumLanePlanManifestAdjuster(49.6),
            TieredLanePlanManifestAdjuster(
                mutableListOf<Double>(3828.0, 5487.0, 6088.0, 7497.0),
                mutableListOf<Double>(0.81, 0.83, 1.08, 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 14562.9,
    )
}
