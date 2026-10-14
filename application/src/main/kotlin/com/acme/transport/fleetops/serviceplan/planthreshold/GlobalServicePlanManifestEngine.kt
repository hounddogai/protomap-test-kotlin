package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.kit.flags.Registry

/** One step of the global service plan manifest calculation. */
interface GlobalServicePlanManifestAdjuster {
    fun describe(): String
    fun adjust(item: GlobalServicePlanManifest, value: Double): Double
}

data class RegionGlobalServicePlanManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalServicePlanManifestAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: GlobalServicePlanManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedGlobalServicePlanManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalServicePlanManifestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalServicePlanManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class GlobalServicePlanManifestAmountOffsetAdjuster(
    var weight: Double,
) : GlobalServicePlanManifestAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: GlobalServicePlanManifest, value: Double): Double = value + item.amount * weight
}

data class CapGlobalServicePlanManifestAdjuster(var cap: Double) : GlobalServicePlanManifestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalServicePlanManifest, value: Double): Double = minOf(value, cap)
}

data class TieredGlobalServicePlanManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalServicePlanManifestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalServicePlanManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global service plan manifest steps in order and keeps a trace of each. */
data class GlobalServicePlanManifestEngine(
    var steps: MutableList<GlobalServicePlanManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalServicePlanManifest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalServicePlanManifestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalServicePlanManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalServicePlanManifestEngine(flags: Registry): GlobalServicePlanManifestEngine {
    return GlobalServicePlanManifestEngine(
        steps = mutableListOf<GlobalServicePlanManifestAdjuster>(
            RegionGlobalServicePlanManifestAdjuster(
                mutableMapOf<String, Double>("online" to 0.71, "south" to 0.88),
            ),
            FlaggedGlobalServicePlanManifestAdjuster(flags, "global-service-plan-manifest-scheduled", 4.1),
            GlobalServicePlanManifestAmountOffsetAdjuster(0.04),
            CapGlobalServicePlanManifestAdjuster(19985.0),
            TieredGlobalServicePlanManifestAdjuster(
                mutableListOf<Double>(3833.0, 4040.0, 8838.0),
                mutableListOf<Double>(1.08, 0.98, 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 49821.2,
    )
}
