package com.acme.transport.fleetops.leasereturn.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lease return bundle calculation. */
interface LeaseReturnBundleAdjuster {
    fun describe(): String
    fun adjust(item: LeaseReturnBundle, value: Double): Double
}

data class LeaseReturnBundleRetriesOffsetAdjuster(var weight: Double) : LeaseReturnBundleAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: LeaseReturnBundle, value: Double): Double = value + item.retries.toDouble() * weight
}

data class PercentLeaseReturnBundleAdjuster(var percent: Double) : LeaseReturnBundleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LeaseReturnBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLeaseReturnBundleAdjuster(var minimum: Double) : LeaseReturnBundleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LeaseReturnBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLeaseReturnBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LeaseReturnBundleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LeaseReturnBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lease return bundle steps in order and keeps a trace of each. */
data class LeaseReturnBundleEngine(
    var steps: MutableList<LeaseReturnBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LeaseReturnBundle): Double {
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

    fun evaluateAll(items: MutableList<LeaseReturnBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLeaseReturnBundleEngine(flags: Registry): LeaseReturnBundleEngine {
    return LeaseReturnBundleEngine(
        steps = mutableListOf<LeaseReturnBundleAdjuster>(
            LeaseReturnBundleRetriesOffsetAdjuster(0.2),
            PercentLeaseReturnBundleAdjuster(9.8),
            MinimumLeaseReturnBundleAdjuster(3.7),
            FlaggedLeaseReturnBundleAdjuster(flags, "lease-return-bundle-lenient", 9.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 90174.3,
    )
}
