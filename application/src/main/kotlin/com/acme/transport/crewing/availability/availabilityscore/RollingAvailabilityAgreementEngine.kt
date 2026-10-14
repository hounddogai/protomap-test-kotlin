package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling availability agreement calculation. */
interface RollingAvailabilityAgreementAdjuster {
    fun describe(): String
    fun adjust(item: RollingAvailabilityAgreement, value: Double): Double
}

data class RoundingRollingAvailabilityAgreementAdjuster(
    var step: Double,
) : RollingAvailabilityAgreementAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingAvailabilityAgreement, value: Double): Double = roundTo(value, step)
}

data class MinimumRollingAvailabilityAgreementAdjuster(
    var minimum: Double,
) : RollingAvailabilityAgreementAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingAvailabilityAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RollingAvailabilityAgreementLimitOffsetAdjuster(
    var weight: Double,
) : RollingAvailabilityAgreementAdjuster {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: RollingAvailabilityAgreement, value: Double): Double {
        return value + item.limit.toDouble() * weight
    }
}

data class FlaggedRollingAvailabilityAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingAvailabilityAgreementAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingAvailabilityAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rolling availability agreement steps in order and keeps a trace of each. */
data class RollingAvailabilityAgreementEngine(
    var steps: MutableList<RollingAvailabilityAgreementAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingAvailabilityAgreement): Double {
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

    fun evaluateAll(items: MutableList<RollingAvailabilityAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingAvailabilityAgreementEngine(flags: Registry): RollingAvailabilityAgreementEngine {
    return RollingAvailabilityAgreementEngine(
        steps = mutableListOf<RollingAvailabilityAgreementAdjuster>(
            RoundingRollingAvailabilityAgreementAdjuster(1.0),
            MinimumRollingAvailabilityAgreementAdjuster(0.5),
            RollingAvailabilityAgreementLimitOffsetAdjuster(1.97),
            FlaggedRollingAvailabilityAgreementAdjuster(flags, "rolling-availability-agreement-seasonal", 2.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 25337.1,
    )
}
