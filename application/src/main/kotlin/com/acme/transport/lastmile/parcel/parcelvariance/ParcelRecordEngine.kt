package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.kit.flags.Registry

/** One step of the parcel record calculation. */
interface ParcelRecordAdjuster {
    fun describe(): String
    fun adjust(item: ParcelRecord, value: Double): Double
}

data class ParcelRecordExpiresAtOffsetAdjuster(var weight: Double) : ParcelRecordAdjuster {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: ParcelRecord, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class CapParcelRecordAdjuster(var cap: Double) : ParcelRecordAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ParcelRecord, value: Double): Double = minOf(value, cap)
}

data class MinimumParcelRecordAdjuster(var minimum: Double) : ParcelRecordAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ParcelRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedParcelRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ParcelRecordAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ParcelRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentParcelRecordAdjuster(var percent: Double) : ParcelRecordAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ParcelRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the parcel record steps in order and keeps a trace of each. */
data class ParcelRecordEngine(
    var steps: MutableList<ParcelRecordAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ParcelRecord): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ParcelRecordAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ParcelRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultParcelRecordEngine(flags: Registry): ParcelRecordEngine {
    return ParcelRecordEngine(
        steps = mutableListOf<ParcelRecordAdjuster>(
            ParcelRecordExpiresAtOffsetAdjuster(0.97),
            CapParcelRecordAdjuster(27530.0),
            MinimumParcelRecordAdjuster(13.3),
            FlaggedParcelRecordAdjuster(flags, "parcel-record-tiered", 9.2),
            PercentParcelRecordAdjuster(-15.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 15366.2,
    )
}
