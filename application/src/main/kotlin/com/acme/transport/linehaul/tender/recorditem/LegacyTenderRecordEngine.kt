package com.acme.transport.linehaul.tender.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy tender record calculation. */
interface LegacyTenderRecordFactor {
    fun describe(): String
    fun adjust(item: LegacyTenderRecord, value: Double): Double
}

data class TitleLegacyTenderRecordAdjuster(var factors: MutableMap<String, Double>) : LegacyTenderRecordFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: LegacyTenderRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLegacyTenderRecordAdjuster(var step: Double) : LegacyTenderRecordFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyTenderRecord, value: Double): Double = roundTo(value, step)
}

data class PercentLegacyTenderRecordAdjuster(var percent: Double) : LegacyTenderRecordFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyTenderRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLegacyTenderRecordAdjuster(var minimum: Double) : LegacyTenderRecordFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyTenderRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLegacyTenderRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyTenderRecordFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyTenderRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the legacy tender record steps in order and keeps a trace of each. */
data class LegacyTenderRecordEngine(
    var steps: MutableList<LegacyTenderRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyTenderRecord): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyTenderRecordFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyTenderRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyTenderRecordEngine(flags: Registry): LegacyTenderRecordEngine {
    return LegacyTenderRecordEngine(
        steps = mutableListOf<LegacyTenderRecordFactor>(
            TitleLegacyTenderRecordAdjuster(mutableMapOf<String, Double>("west" to 0.71)),
            RoundingLegacyTenderRecordAdjuster(0.1),
            PercentLegacyTenderRecordAdjuster(-24.8),
            MinimumLegacyTenderRecordAdjuster(31.8),
            FlaggedLegacyTenderRecordAdjuster(flags, "legacy-tender-record-tiered", -0.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 92796.1,
    )
}
