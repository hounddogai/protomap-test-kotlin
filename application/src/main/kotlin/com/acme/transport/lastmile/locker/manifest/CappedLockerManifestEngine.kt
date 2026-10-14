package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped locker manifest calculation. */
interface CappedLockerManifestAdjuster {
    fun describe(): String
    fun adjust(item: CappedLockerManifest, value: Double): Double
}

data class CappedLockerManifestThresholdOffsetAdjuster(var weight: Double) : CappedLockerManifestAdjuster {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: CappedLockerManifest, value: Double): Double = value + item.threshold * weight
}

data class PercentCappedLockerManifestAdjuster(var percent: Double) : CappedLockerManifestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedLockerManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCappedLockerManifestAdjuster(var step: Double) : CappedLockerManifestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedLockerManifest, value: Double): Double = roundTo(value, step)
}

data class TieredCappedLockerManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedLockerManifestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedLockerManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedCappedLockerManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedLockerManifestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedLockerManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCappedLockerManifestAdjuster(var cap: Double) : CappedLockerManifestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedLockerManifest, value: Double): Double = minOf(value, cap)
}

/** Runs the capped locker manifest steps in order and keeps a trace of each. */
data class CappedLockerManifestEngine(
    var steps: MutableList<CappedLockerManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedLockerManifest): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedLockerManifestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CappedLockerManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedLockerManifestEngine(flags: Registry): CappedLockerManifestEngine {
    return CappedLockerManifestEngine(
        steps = mutableListOf<CappedLockerManifestAdjuster>(
            CappedLockerManifestThresholdOffsetAdjuster(1.14),
            PercentCappedLockerManifestAdjuster(-3.8),
            RoundingCappedLockerManifestAdjuster(0.5),
            TieredCappedLockerManifestAdjuster(
                mutableListOf<Double>(2386.0, 7567.0, 7792.0),
                mutableListOf<Double>(1.08, 1.17, 1.05),
            ),
            FlaggedCappedLockerManifestAdjuster(flags, "capped-locker-manifest-rolling", 0.7),
            CapCappedLockerManifestAdjuster(12632.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 93758.5,
    )
}
