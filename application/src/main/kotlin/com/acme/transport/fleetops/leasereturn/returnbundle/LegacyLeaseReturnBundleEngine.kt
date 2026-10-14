package com.acme.transport.fleetops.leasereturn.returnbundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy lease return bundle calculation. */
interface LegacyLeaseReturnBundleStep {
    fun describe(): String
    fun adjust(item: LegacyLeaseReturnBundle, value: Double): Double
}

data class MinimumLegacyLeaseReturnBundleAdjuster(var minimum: Double) : LegacyLeaseReturnBundleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyLeaseReturnBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredLegacyLeaseReturnBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyLeaseReturnBundleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyLeaseReturnBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLegacyLeaseReturnBundleAdjuster(var percent: Double) : LegacyLeaseReturnBundleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyLeaseReturnBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLegacyLeaseReturnBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyLeaseReturnBundleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyLeaseReturnBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingLegacyLeaseReturnBundleAdjuster(var step: Double) : LegacyLeaseReturnBundleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyLeaseReturnBundle, value: Double): Double = roundTo(value, step)
}

/** Runs the legacy lease return bundle steps in order and keeps a trace of each. */
data class LegacyLeaseReturnBundleEngine(
    var steps: MutableList<LegacyLeaseReturnBundleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyLeaseReturnBundle): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyLeaseReturnBundleStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyLeaseReturnBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyLeaseReturnBundleEngine(flags: Registry): LegacyLeaseReturnBundleEngine {
    return LegacyLeaseReturnBundleEngine(
        steps = mutableListOf<LegacyLeaseReturnBundleStep>(
            MinimumLegacyLeaseReturnBundleAdjuster(14.7),
            TieredLegacyLeaseReturnBundleAdjuster(
                mutableListOf<Double>(2212.0, 6056.0),
                mutableListOf<Double>(1.13, 0.88),
            ),
            PercentLegacyLeaseReturnBundleAdjuster(23.5),
            FlaggedLegacyLeaseReturnBundleAdjuster(flags, "legacy-lease-return-bundle-pending", 0.9),
            RoundingLegacyLeaseReturnBundleAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 2639.6,
    )
}
