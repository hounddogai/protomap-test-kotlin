package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the locker manifest calculation. */
interface LockerManifestStep {
    fun describe(): String
    fun adjust(item: LockerManifest, value: Double): Double
}

data class RoundingLockerManifestAdjuster(var step: Double) : LockerManifestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LockerManifest, value: Double): Double = roundTo(value, step)
}

data class TieredLockerManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LockerManifestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LockerManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapLockerManifestAdjuster(var cap: Double) : LockerManifestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LockerManifest, value: Double): Double = minOf(value, cap)
}

data class LockerManifestAmountOffsetAdjuster(var weight: Double) : LockerManifestStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: LockerManifest, value: Double): Double = value + item.amount * weight
}

data class FlaggedLockerManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LockerManifestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LockerManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the locker manifest steps in order and keeps a trace of each. */
data class LockerManifestEngine(
    var steps: MutableList<LockerManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LockerManifest): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LockerManifestStep = steps[index]
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

    fun evaluateAll(items: MutableList<LockerManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLockerManifestEngine(flags: Registry): LockerManifestEngine {
    return LockerManifestEngine(
        steps = mutableListOf<LockerManifestStep>(
            RoundingLockerManifestAdjuster(0.25),
            TieredLockerManifestAdjuster(
                mutableListOf<Double>(495.0, 2193.0, 8192.0, 8342.0),
                mutableListOf<Double>(0.98, 1.06, 1.11, 0.84),
            ),
            CapLockerManifestAdjuster(32671.0),
            LockerManifestAmountOffsetAdjuster(0.8),
            FlaggedLockerManifestAdjuster(flags, "locker-manifest-seasonal", 8.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 17516.2,
    )
}
