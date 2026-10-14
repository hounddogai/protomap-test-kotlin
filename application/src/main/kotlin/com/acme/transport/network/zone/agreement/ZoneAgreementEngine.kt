package com.acme.transport.network.zone.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the zone agreement calculation. */
interface ZoneAgreementFactor {
    fun describe(): String
    fun adjust(item: ZoneAgreement, value: Double): Double
}

data class FlaggedZoneAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ZoneAgreementFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ZoneAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumZoneAgreementAdjuster(var minimum: Double) : ZoneAgreementFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ZoneAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingZoneAgreementAdjuster(var step: Double) : ZoneAgreementFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ZoneAgreement, value: Double): Double = roundTo(value, step)
}

/** Runs the zone agreement steps in order and keeps a trace of each. */
data class ZoneAgreementEngine(
    var steps: MutableList<ZoneAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ZoneAgreement): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ZoneAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultZoneAgreementEngine(flags: Registry): ZoneAgreementEngine {
    return ZoneAgreementEngine(
        steps = mutableListOf<ZoneAgreementFactor>(
            FlaggedZoneAgreementAdjuster(flags, "zone-agreement-legacy", -2.4),
            MinimumZoneAgreementAdjuster(46.6),
            RoundingZoneAgreementAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 23711.0,
    )
}
