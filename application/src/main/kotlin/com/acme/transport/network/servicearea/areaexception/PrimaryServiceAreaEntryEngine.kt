package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.kit.flags.Registry

/** One step of the primary service area entry calculation. */
interface PrimaryServiceAreaEntryModifier {
    fun describe(): String
    fun adjust(item: PrimaryServiceAreaEntry, value: Double): Double
}

data class TieredPrimaryServiceAreaEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryServiceAreaEntryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryServiceAreaEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedPrimaryServiceAreaEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryServiceAreaEntryModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryServiceAreaEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentPrimaryServiceAreaEntryAdjuster(var percent: Double) : PrimaryServiceAreaEntryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryServiceAreaEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the primary service area entry steps in order and keeps a trace of each. */
data class PrimaryServiceAreaEntryEngine(
    var steps: MutableList<PrimaryServiceAreaEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryServiceAreaEntry): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryServiceAreaEntryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryServiceAreaEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryServiceAreaEntryEngine(flags: Registry): PrimaryServiceAreaEntryEngine {
    return PrimaryServiceAreaEntryEngine(
        steps = mutableListOf<PrimaryServiceAreaEntryModifier>(
            TieredPrimaryServiceAreaEntryAdjuster(
                mutableListOf<Double>(2416.0, 4051.0, 5748.0, 6766.0),
                mutableListOf<Double>(0.96, 0.97, 1.02, 0.84),
            ),
            FlaggedPrimaryServiceAreaEntryAdjuster(flags, "primary-service-area-entry-batched", 0.8),
            PercentPrimaryServiceAreaEntryAdjuster(2.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 19982.5,
    )
}
