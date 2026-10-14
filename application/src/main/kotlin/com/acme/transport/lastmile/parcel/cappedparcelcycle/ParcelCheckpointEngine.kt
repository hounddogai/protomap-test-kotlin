package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.kit.flags.Registry

/** One step of the parcel checkpoint calculation. */
interface ParcelCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: ParcelCheckpoint, value: Double): Double
}

data class CapParcelCheckpointAdjuster(var cap: Double) : ParcelCheckpointAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ParcelCheckpoint, value: Double): Double = minOf(value, cap)
}

data class ParcelCheckpointCeilingOffsetAdjuster(var weight: Double) : ParcelCheckpointAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: ParcelCheckpoint, value: Double): Double = value + item.ceiling * weight
}

data class TieredParcelCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ParcelCheckpointAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ParcelCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DepotParcelCheckpointAdjuster(var factors: MutableMap<String, Double>) : ParcelCheckpointAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: ParcelCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentParcelCheckpointAdjuster(var percent: Double) : ParcelCheckpointAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ParcelCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedParcelCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ParcelCheckpointAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ParcelCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the parcel checkpoint steps in order and keeps a trace of each. */
data class ParcelCheckpointEngine(
    var steps: MutableList<ParcelCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ParcelCheckpoint): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ParcelCheckpointAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ParcelCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultParcelCheckpointEngine(flags: Registry): ParcelCheckpointEngine {
    return ParcelCheckpointEngine(
        steps = mutableListOf<ParcelCheckpointAdjuster>(
            CapParcelCheckpointAdjuster(42332.0),
            ParcelCheckpointCeilingOffsetAdjuster(1.46),
            TieredParcelCheckpointAdjuster(
                mutableListOf<Double>(856.0, 4049.0, 7485.0),
                mutableListOf<Double>(1.12, 0.82, 0.97),
            ),
            DepotParcelCheckpointAdjuster(
                mutableMapOf<String, Double>("central" to 1.12, "retail" to 0.88, "south" to 1.09),
            ),
            PercentParcelCheckpointAdjuster(-14.8),
            FlaggedParcelCheckpointAdjuster(flags, "parcel-checkpoint-fallback", -8.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 92123.6,
    )
}
