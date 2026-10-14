package com.acme.transport.safety.incident.globalincidentproposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary incident claim calculation. */
interface PrimaryIncidentClaimStep {
    fun describe(): String
    fun adjust(item: PrimaryIncidentClaim, value: Double): Double
}

data class FlaggedPrimaryIncidentClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryIncidentClaimStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryIncidentClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PrimaryIncidentClaimRateOffsetAdjuster(var weight: Double) : PrimaryIncidentClaimStep {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: PrimaryIncidentClaim, value: Double): Double = value + item.rate * weight
}

data class CountryPrimaryIncidentClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryIncidentClaimStep {
    override fun describe(): String = "by country"

    override fun adjust(item: PrimaryIncidentClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingPrimaryIncidentClaimAdjuster(var step: Double) : PrimaryIncidentClaimStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryIncidentClaim, value: Double): Double = roundTo(value, step)
}

/** Runs the primary incident claim steps in order and keeps a trace of each. */
data class PrimaryIncidentClaimEngine(
    var steps: MutableList<PrimaryIncidentClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryIncidentClaim): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryIncidentClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryIncidentClaimEngine(flags: Registry): PrimaryIncidentClaimEngine {
    return PrimaryIncidentClaimEngine(
        steps = mutableListOf<PrimaryIncidentClaimStep>(
            FlaggedPrimaryIncidentClaimAdjuster(flags, "primary-incident-claim-capped", -3.7),
            PrimaryIncidentClaimRateOffsetAdjuster(0.59),
            CountryPrimaryIncidentClaimAdjuster(
                mutableMapOf<String, Double>("south" to 1.19, "central" to 1.09, "online" to 1.16),
            ),
            RoundingPrimaryIncidentClaimAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 44069.2,
    )
}
