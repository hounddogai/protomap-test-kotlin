package com.acme.transport.linehaul.capacity.request

import com.acme.transport.kit.flags.Registry

/** One step of the capacity record calculation. */
interface CapacityRecordModifier {
    fun describe(): String
    fun adjust(item: CapacityRecord, value: Double): Double
}

data class PercentCapacityRecordAdjuster(var percent: Double) : CapacityRecordModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CapacityRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCapacityRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CapacityRecordModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CapacityRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCapacityRecordAdjuster(var cap: Double) : CapacityRecordModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacityRecord, value: Double): Double = minOf(value, cap)
}

data class TieredCapacityRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CapacityRecordModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CapacityRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapacityRecordMinimumOffsetAdjuster(var weight: Double) : CapacityRecordModifier {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: CapacityRecord, value: Double): Double = value + item.minimum * weight
}

/** Runs the capacity record steps in order and keeps a trace of each. */
data class CapacityRecordEngine(
    var steps: MutableList<CapacityRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CapacityRecord): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CapacityRecordModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CapacityRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCapacityRecordEngine(flags: Registry): CapacityRecordEngine {
    return CapacityRecordEngine(
        steps = mutableListOf<CapacityRecordModifier>(
            PercentCapacityRecordAdjuster(18.0),
            FlaggedCapacityRecordAdjuster(flags, "capacity-record-adaptive", 0.1),
            CapCapacityRecordAdjuster(13357.0),
            TieredCapacityRecordAdjuster(
                mutableListOf<Double>(1289.0, 2809.0),
                mutableListOf<Double>(0.85, 1.02),
            ),
            CapacityRecordMinimumOffsetAdjuster(1.93),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 38106.6,
    )
}
