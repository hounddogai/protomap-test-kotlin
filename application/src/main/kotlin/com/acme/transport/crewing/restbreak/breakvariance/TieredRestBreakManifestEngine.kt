package com.acme.transport.crewing.restbreak.breakvariance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered rest break manifest calculation. */
interface TieredRestBreakManifestModifier {
    fun describe(): String
    fun adjust(item: TieredRestBreakManifest, value: Double): Double
}

data class TieredRestBreakManifestOdometerOffsetAdjuster(var weight: Double) : TieredRestBreakManifestModifier {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: TieredRestBreakManifest, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class RoundingTieredRestBreakManifestAdjuster(var step: Double) : TieredRestBreakManifestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredRestBreakManifest, value: Double): Double = roundTo(value, step)
}

data class MinimumTieredRestBreakManifestAdjuster(var minimum: Double) : TieredRestBreakManifestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredRestBreakManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameTieredRestBreakManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredRestBreakManifestModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: TieredRestBreakManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedTieredRestBreakManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredRestBreakManifestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredRestBreakManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tiered rest break manifest steps in order and keeps a trace of each. */
data class TieredRestBreakManifestEngine(
    var steps: MutableList<TieredRestBreakManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredRestBreakManifest): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredRestBreakManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredRestBreakManifestEngine(flags: Registry): TieredRestBreakManifestEngine {
    return TieredRestBreakManifestEngine(
        steps = mutableListOf<TieredRestBreakManifestModifier>(
            TieredRestBreakManifestOdometerOffsetAdjuster(0.6),
            RoundingTieredRestBreakManifestAdjuster(0.1),
            MinimumTieredRestBreakManifestAdjuster(37.3),
            NameTieredRestBreakManifestAdjuster(
                mutableMapOf<String, Double>("online" to 1.09, "retail" to 1.18),
            ),
            FlaggedTieredRestBreakManifestAdjuster(flags, "tiered-rest-break-manifest-seasonal", 9.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 83813.8,
    )
}
