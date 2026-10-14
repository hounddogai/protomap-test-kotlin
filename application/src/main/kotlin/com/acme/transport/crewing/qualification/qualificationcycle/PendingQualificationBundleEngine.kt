package com.acme.transport.crewing.qualification.qualificationcycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending qualification bundle calculation. */
interface PendingQualificationBundleModifier {
    fun describe(): String
    fun adjust(item: PendingQualificationBundle, value: Double): Double
}

data class CapPendingQualificationBundleAdjuster(var cap: Double) : PendingQualificationBundleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingQualificationBundle, value: Double): Double = minOf(value, cap)
}

data class RoundingPendingQualificationBundleAdjuster(var step: Double) : PendingQualificationBundleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingQualificationBundle, value: Double): Double = roundTo(value, step)
}

data class FlaggedPendingQualificationBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingQualificationBundleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingQualificationBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PendingQualificationBundleScoreOffsetAdjuster(
    var weight: Double,
) : PendingQualificationBundleModifier {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: PendingQualificationBundle, value: Double): Double = value + item.score * weight
}

data class TieredPendingQualificationBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingQualificationBundleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingQualificationBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the pending qualification bundle steps in order and keeps a trace of each. */
data class PendingQualificationBundleEngine(
    var steps: MutableList<PendingQualificationBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingQualificationBundle): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingQualificationBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingQualificationBundleEngine(flags: Registry): PendingQualificationBundleEngine {
    return PendingQualificationBundleEngine(
        steps = mutableListOf<PendingQualificationBundleModifier>(
            CapPendingQualificationBundleAdjuster(43807.0),
            RoundingPendingQualificationBundleAdjuster(0.1),
            FlaggedPendingQualificationBundleAdjuster(flags, "pending-qualification-bundle-default", 4.3),
            PendingQualificationBundleScoreOffsetAdjuster(0.37),
            TieredPendingQualificationBundleAdjuster(
                mutableListOf<Double>(3541.0, 5750.0),
                mutableListOf<Double>(0.96, 0.88),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 95685.0,
    )
}
