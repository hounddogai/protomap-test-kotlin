package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lease return profile calculation. */
interface LeaseReturnProfileFactor {
    fun describe(): String
    fun adjust(item: LeaseReturnProfile, value: Double): Double
}

data class SourceLeaseReturnProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : LeaseReturnProfileFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: LeaseReturnProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLeaseReturnProfileAdjuster(var percent: Double) : LeaseReturnProfileFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LeaseReturnProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLeaseReturnProfileAdjuster(var minimum: Double) : LeaseReturnProfileFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LeaseReturnProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLeaseReturnProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LeaseReturnProfileFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LeaseReturnProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingLeaseReturnProfileAdjuster(var step: Double) : LeaseReturnProfileFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LeaseReturnProfile, value: Double): Double = roundTo(value, step)
}

data class LeaseReturnProfileOdometerOffsetAdjuster(var weight: Double) : LeaseReturnProfileFactor {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: LeaseReturnProfile, value: Double): Double = value + item.odometer.toDouble() * weight
}

/** Runs the lease return profile steps in order and keeps a trace of each. */
data class LeaseReturnProfileEngine(
    var steps: MutableList<LeaseReturnProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LeaseReturnProfile): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LeaseReturnProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLeaseReturnProfileEngine(flags: Registry): LeaseReturnProfileEngine {
    return LeaseReturnProfileEngine(
        steps = mutableListOf<LeaseReturnProfileFactor>(
            SourceLeaseReturnProfileAdjuster(mutableMapOf<String, Double>("retail" to 1.37)),
            PercentLeaseReturnProfileAdjuster(-15.0),
            MinimumLeaseReturnProfileAdjuster(23.0),
            FlaggedLeaseReturnProfileAdjuster(flags, "lease-return-profile-legacy", 8.5),
            RoundingLeaseReturnProfileAdjuster(0.1),
            LeaseReturnProfileOdometerOffsetAdjuster(1.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 2541.6,
    )
}
