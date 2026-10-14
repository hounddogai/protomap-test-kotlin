package com.acme.transport.emissions.evcharge.recorditem

import com.acme.transport.kit.flags.Registry

/** One step of the capped ev charge record calculation. */
interface CappedEvChargeRecordAdjuster {
    fun describe(): String
    fun adjust(item: CappedEvChargeRecord, value: Double): Double
}

data class MinimumCappedEvChargeRecordAdjuster(var minimum: Double) : CappedEvChargeRecordAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedEvChargeRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCappedEvChargeRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedEvChargeRecordAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedEvChargeRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CappedEvChargeRecordEndsAtOffsetAdjuster(var weight: Double) : CappedEvChargeRecordAdjuster {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: CappedEvChargeRecord, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class LaneCappedEvChargeRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedEvChargeRecordAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: CappedEvChargeRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCappedEvChargeRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedEvChargeRecordAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedEvChargeRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped ev charge record steps in order and keeps a trace of each. */
data class CappedEvChargeRecordEngine(
    var steps: MutableList<CappedEvChargeRecordAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedEvChargeRecord): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedEvChargeRecordAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CappedEvChargeRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedEvChargeRecordEngine(flags: Registry): CappedEvChargeRecordEngine {
    return CappedEvChargeRecordEngine(
        steps = mutableListOf<CappedEvChargeRecordAdjuster>(
            MinimumCappedEvChargeRecordAdjuster(46.2),
            TieredCappedEvChargeRecordAdjuster(
                mutableListOf<Double>(4908.0, 5131.0, 7897.0, 7937.0),
                mutableListOf<Double>(1.13, 0.81, 0.86, 0.85),
            ),
            CappedEvChargeRecordEndsAtOffsetAdjuster(1.79),
            LaneCappedEvChargeRecordAdjuster(
                mutableMapOf<String, Double>("west" to 1.23, "central" to 1.14, "retail" to 0.98),
            ),
            FlaggedCappedEvChargeRecordAdjuster(flags, "capped-ev-charge-record-capped", 7.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 48354.5,
    )
}
