package com.acme.transport.crewing.qualification.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary qualification quota calculation. */
interface PrimaryQualificationQuotaAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryQualificationQuota, value: Double): Double
}

data class RoundingPrimaryQualificationQuotaAdjuster(var step: Double) : PrimaryQualificationQuotaAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryQualificationQuota, value: Double): Double = roundTo(value, step)
}

data class FlaggedPrimaryQualificationQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryQualificationQuotaAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryQualificationQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPrimaryQualificationQuotaAdjuster(var minimum: Double) : PrimaryQualificationQuotaAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryQualificationQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredPrimaryQualificationQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryQualificationQuotaAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryQualificationQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary qualification quota steps in order and keeps a trace of each. */
data class PrimaryQualificationQuotaEngine(
    var steps: MutableList<PrimaryQualificationQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryQualificationQuota): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryQualificationQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryQualificationQuotaEngine(flags: Registry): PrimaryQualificationQuotaEngine {
    return PrimaryQualificationQuotaEngine(
        steps = mutableListOf<PrimaryQualificationQuotaAdjuster>(
            RoundingPrimaryQualificationQuotaAdjuster(1.0),
            FlaggedPrimaryQualificationQuotaAdjuster(flags, "primary-qualification-quota-lenient", 3.9),
            MinimumPrimaryQualificationQuotaAdjuster(14.0),
            TieredPrimaryQualificationQuotaAdjuster(
                mutableListOf<Double>(4509.0, 6423.0, 7873.0),
                mutableListOf<Double>(1.1, 1.11, 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 88006.9,
    )
}
