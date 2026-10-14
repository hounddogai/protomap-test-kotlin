package com.acme.transport.network.zone.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped zone claim calculation. */
interface CappedZoneClaimAdjuster {
    fun describe(): String
    fun adjust(item: CappedZoneClaim, value: Double): Double
}

data class NameCappedZoneClaimAdjuster(var factors: MutableMap<String, Double>) : CappedZoneClaimAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: CappedZoneClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapCappedZoneClaimAdjuster(var cap: Double) : CappedZoneClaimAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedZoneClaim, value: Double): Double = minOf(value, cap)
}

data class FlaggedCappedZoneClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedZoneClaimAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedZoneClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped zone claim steps in order and keeps a trace of each. */
data class CappedZoneClaimEngine(
    var steps: MutableList<CappedZoneClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedZoneClaim): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedZoneClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedZoneClaimEngine(flags: Registry): CappedZoneClaimEngine {
    return CappedZoneClaimEngine(
        steps = mutableListOf<CappedZoneClaimAdjuster>(
            NameCappedZoneClaimAdjuster(mutableMapOf<String, Double>("west" to 1.39)),
            CapCappedZoneClaimAdjuster(14158.0),
            FlaggedCappedZoneClaimAdjuster(flags, "capped-zone-claim-default", -2.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 49201.2,
    )
}
