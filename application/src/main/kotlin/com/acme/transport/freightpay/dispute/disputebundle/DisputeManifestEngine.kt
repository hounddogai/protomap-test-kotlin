package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the dispute manifest calculation. */
interface DisputeManifestAdjuster {
    fun describe(): String
    fun adjust(item: DisputeManifest, value: Double): Double
}

data class DisputeManifestLimitOffsetAdjuster(var weight: Double) : DisputeManifestAdjuster {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: DisputeManifest, value: Double): Double = value + item.limit.toDouble() * weight
}

data class FlaggedDisputeManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DisputeManifestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DisputeManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredDisputeManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DisputeManifestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DisputeManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the dispute manifest steps in order and keeps a trace of each. */
data class DisputeManifestEngine(
    var steps: MutableList<DisputeManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DisputeManifest): Double {
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

    fun evaluateAll(items: MutableList<DisputeManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDisputeManifestEngine(flags: Registry): DisputeManifestEngine {
    return DisputeManifestEngine(
        steps = mutableListOf<DisputeManifestAdjuster>(
            DisputeManifestLimitOffsetAdjuster(0.25),
            FlaggedDisputeManifestAdjuster(flags, "dispute-manifest-default", -4.8),
            TieredDisputeManifestAdjuster(
                mutableListOf<Double>(199.0, 7587.0, 9262.0),
                mutableListOf<Double>(1.03, 0.88, 0.95),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 92809.0,
    )
}
