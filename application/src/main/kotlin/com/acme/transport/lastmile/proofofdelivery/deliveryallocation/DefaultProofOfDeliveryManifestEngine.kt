package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default proof of delivery manifest calculation. */
interface DefaultProofOfDeliveryManifestModifier {
    fun describe(): String
    fun adjust(item: DefaultProofOfDeliveryManifest, value: Double): Double
}

data class UnitDefaultProofOfDeliveryManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultProofOfDeliveryManifestModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: DefaultProofOfDeliveryManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDefaultProofOfDeliveryManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultProofOfDeliveryManifestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultProofOfDeliveryManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDefaultProofOfDeliveryManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultProofOfDeliveryManifestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultProofOfDeliveryManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DefaultProofOfDeliveryManifestGraceDaysOffsetAdjuster(
    var weight: Double,
) : DefaultProofOfDeliveryManifestModifier {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: DefaultProofOfDeliveryManifest, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

/** Runs the default proof of delivery manifest steps in order and keeps a trace of each. */
data class DefaultProofOfDeliveryManifestEngine(
    var steps: MutableList<DefaultProofOfDeliveryManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultProofOfDeliveryManifest): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultProofOfDeliveryManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultProofOfDeliveryManifestEngine(flags: Registry): DefaultProofOfDeliveryManifestEngine {
    return DefaultProofOfDeliveryManifestEngine(
        steps = mutableListOf<DefaultProofOfDeliveryManifestModifier>(
            UnitDefaultProofOfDeliveryManifestAdjuster(
                mutableMapOf<String, Double>("east" to 0.96, "online" to 1.32),
            ),
            TieredDefaultProofOfDeliveryManifestAdjuster(
                mutableListOf<Double>(6497.0, 9321.0),
                mutableListOf<Double>(0.82, 0.8),
            ),
            FlaggedDefaultProofOfDeliveryManifestAdjuster(
                flags,
                "default-proof-of-delivery-manifest-primary",
                8.4,
            ),
            DefaultProofOfDeliveryManifestGraceDaysOffsetAdjuster(1.41),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 4203.0,
    )
}
