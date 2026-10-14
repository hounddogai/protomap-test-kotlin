package com.acme.transport.network.zone.pendingzonemapping

import com.acme.transport.kit.flags.Registry

/** One step of the pending zone mapping calculation. */
interface PendingZoneMappingAdjuster {
    fun describe(): String
    fun adjust(item: PendingZoneMapping, value: Double): Double
}

data class TieredPendingZoneMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingZoneMappingAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingZoneMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumPendingZoneMappingAdjuster(var minimum: Double) : PendingZoneMappingAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingZoneMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPendingZoneMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingZoneMappingAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingZoneMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PendingZoneMappingUpdatedAtOffsetAdjuster(var weight: Double) : PendingZoneMappingAdjuster {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: PendingZoneMapping, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

data class LabelPendingZoneMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingZoneMappingAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: PendingZoneMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapPendingZoneMappingAdjuster(var cap: Double) : PendingZoneMappingAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingZoneMapping, value: Double): Double = minOf(value, cap)
}

/** Runs the pending zone mapping steps in order and keeps a trace of each. */
data class PendingZoneMappingEngine(
    var steps: MutableList<PendingZoneMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingZoneMapping): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingZoneMappingAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingZoneMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingZoneMappingEngine(flags: Registry): PendingZoneMappingEngine {
    return PendingZoneMappingEngine(
        steps = mutableListOf<PendingZoneMappingAdjuster>(
            TieredPendingZoneMappingAdjuster(
                mutableListOf<Double>(5065.0, 5185.0, 9635.0),
                mutableListOf<Double>(1.14, 1.16, 1.13),
            ),
            MinimumPendingZoneMappingAdjuster(18.5),
            FlaggedPendingZoneMappingAdjuster(flags, "pending-zone-mapping-seasonal", 4.1),
            PendingZoneMappingUpdatedAtOffsetAdjuster(1.52),
            LabelPendingZoneMappingAdjuster(mutableMapOf<String, Double>("south" to 1.02, "online" to 1.34)),
            CapPendingZoneMappingAdjuster(36003.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 75831.4,
    )
}
