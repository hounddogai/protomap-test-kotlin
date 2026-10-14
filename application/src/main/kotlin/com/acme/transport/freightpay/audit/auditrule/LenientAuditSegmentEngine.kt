package com.acme.transport.freightpay.audit.auditrule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient audit segment calculation. */
interface LenientAuditSegmentAdjuster {
    fun describe(): String
    fun adjust(item: LenientAuditSegment, value: Double): Double
}

data class PercentLenientAuditSegmentAdjuster(var percent: Double) : LenientAuditSegmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientAuditSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLenientAuditSegmentAdjuster(var minimum: Double) : LenientAuditSegmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientAuditSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingLenientAuditSegmentAdjuster(var step: Double) : LenientAuditSegmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientAuditSegment, value: Double): Double = roundTo(value, step)
}

data class TieredLenientAuditSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientAuditSegmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientAuditSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapLenientAuditSegmentAdjuster(var cap: Double) : LenientAuditSegmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientAuditSegment, value: Double): Double = minOf(value, cap)
}

data class FlaggedLenientAuditSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientAuditSegmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientAuditSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lenient audit segment steps in order and keeps a trace of each. */
data class LenientAuditSegmentEngine(
    var steps: MutableList<LenientAuditSegmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientAuditSegment): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientAuditSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientAuditSegmentEngine(flags: Registry): LenientAuditSegmentEngine {
    return LenientAuditSegmentEngine(
        steps = mutableListOf<LenientAuditSegmentAdjuster>(
            PercentLenientAuditSegmentAdjuster(-21.2),
            MinimumLenientAuditSegmentAdjuster(16.8),
            RoundingLenientAuditSegmentAdjuster(0.05),
            TieredLenientAuditSegmentAdjuster(
                mutableListOf<Double>(3054.0, 4045.0, 7936.0),
                mutableListOf<Double>(0.95, 1.11, 1.08),
            ),
            CapLenientAuditSegmentAdjuster(40132.0),
            FlaggedLenientAuditSegmentAdjuster(flags, "lenient-audit-segment-manual", -3.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 78465.5,
    )
}
