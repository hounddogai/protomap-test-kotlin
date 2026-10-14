package com.acme.transport.crewing.availability.availabilitydigest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy availability digest calculation. */
interface LegacyAvailabilityDigestStep {
    fun describe(): String
    fun adjust(item: LegacyAvailabilityDigest, value: Double): Double
}

data class PercentLegacyAvailabilityDigestAdjuster(var percent: Double) : LegacyAvailabilityDigestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyAvailabilityDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLegacyAvailabilityDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyAvailabilityDigestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyAvailabilityDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DepotLegacyAvailabilityDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyAvailabilityDigestStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: LegacyAvailabilityDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLegacyAvailabilityDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyAvailabilityDigestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyAvailabilityDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LegacyAvailabilityDigestSequenceOffsetAdjuster(var weight: Double) : LegacyAvailabilityDigestStep {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: LegacyAvailabilityDigest, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

/** Runs the legacy availability digest steps in order and keeps a trace of each. */
data class LegacyAvailabilityDigestEngine(
    var steps: MutableList<LegacyAvailabilityDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyAvailabilityDigest): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyAvailabilityDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyAvailabilityDigestEngine(flags: Registry): LegacyAvailabilityDigestEngine {
    return LegacyAvailabilityDigestEngine(
        steps = mutableListOf<LegacyAvailabilityDigestStep>(
            PercentLegacyAvailabilityDigestAdjuster(-24.6),
            FlaggedLegacyAvailabilityDigestAdjuster(flags, "legacy-availability-digest-default", -0.1),
            DepotLegacyAvailabilityDigestAdjuster(mutableMapOf<String, Double>("south" to 1.25, "west" to 1.11)),
            TieredLegacyAvailabilityDigestAdjuster(
                mutableListOf<Double>(962.0, 5469.0, 5912.0),
                mutableListOf<Double>(1.14, 1.09, 0.99),
            ),
            LegacyAvailabilityDigestSequenceOffsetAdjuster(1.33),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 81807.8,
    )
}
