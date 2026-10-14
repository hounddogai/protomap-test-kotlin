package com.acme.transport.customs.hold.slot

import com.acme.transport.kit.flags.Registry

/** One step of the hold manifest calculation. */
interface HoldManifestFactor {
    fun describe(): String
    fun adjust(item: HoldManifest, value: Double): Double
}

data class CapHoldManifestAdjuster(var cap: Double) : HoldManifestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HoldManifest, value: Double): Double = minOf(value, cap)
}

data class MinimumHoldManifestAdjuster(var minimum: Double) : HoldManifestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HoldManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class HoldManifestFuelLitersOffsetAdjuster(var weight: Double) : HoldManifestFactor {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: HoldManifest, value: Double): Double = value + item.fuelLiters * weight
}

data class TieredHoldManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HoldManifestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HoldManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedHoldManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HoldManifestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HoldManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ChannelHoldManifestAdjuster(var factors: MutableMap<String, Double>) : HoldManifestFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: HoldManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the hold manifest steps in order and keeps a trace of each. */
data class HoldManifestEngine(
    var steps: MutableList<HoldManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: HoldManifest): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HoldManifestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<HoldManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultHoldManifestEngine(flags: Registry): HoldManifestEngine {
    return HoldManifestEngine(
        steps = mutableListOf<HoldManifestFactor>(
            CapHoldManifestAdjuster(19933.0),
            MinimumHoldManifestAdjuster(15.3),
            HoldManifestFuelLitersOffsetAdjuster(1.35),
            TieredHoldManifestAdjuster(mutableListOf<Double>(1703.0, 6375.0), mutableListOf<Double>(1.11, 1.08)),
            FlaggedHoldManifestAdjuster(flags, "hold-manifest-tiered", 1.9),
            ChannelHoldManifestAdjuster(mutableMapOf<String, Double>("south" to 0.87)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 32824.3,
    )
}
