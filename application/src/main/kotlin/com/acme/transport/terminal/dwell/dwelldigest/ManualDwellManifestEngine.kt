package com.acme.transport.terminal.dwell.dwelldigest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual dwell manifest calculation. */
interface ManualDwellManifestStep {
    fun describe(): String
    fun adjust(item: ManualDwellManifest, value: Double): Double
}

data class TieredManualDwellManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualDwellManifestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualDwellManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedManualDwellManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualDwellManifestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualDwellManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingManualDwellManifestAdjuster(var step: Double) : ManualDwellManifestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualDwellManifest, value: Double): Double = roundTo(value, step)
}

/** Runs the manual dwell manifest steps in order and keeps a trace of each. */
data class ManualDwellManifestEngine(
    var steps: MutableList<ManualDwellManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualDwellManifest): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualDwellManifestStep = steps[index]
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

    fun evaluateAll(items: MutableList<ManualDwellManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualDwellManifestEngine(flags: Registry): ManualDwellManifestEngine {
    return ManualDwellManifestEngine(
        steps = mutableListOf<ManualDwellManifestStep>(
            TieredManualDwellManifestAdjuster(
                mutableListOf<Double>(2085.0, 3548.0, 5899.0, 8117.0),
                mutableListOf<Double>(1.11, 0.99, 0.88, 1.17),
            ),
            FlaggedManualDwellManifestAdjuster(flags, "manual-dwell-manifest-global", -9.4),
            RoundingManualDwellManifestAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 48425.1,
    )
}
