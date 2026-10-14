package com.acme.transport.customs.broker.quota

import com.acme.transport.kit.flags.Registry

/** One step of the tiered broker manifest calculation. */
interface TieredBrokerManifestModifier {
    fun describe(): String
    fun adjust(item: TieredBrokerManifest, value: Double): Double
}

data class TieredTieredBrokerManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredBrokerManifestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredBrokerManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleTieredBrokerManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredBrokerManifestModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: TieredBrokerManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedTieredBrokerManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredBrokerManifestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredBrokerManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tiered broker manifest steps in order and keeps a trace of each. */
data class TieredBrokerManifestEngine(
    var steps: MutableList<TieredBrokerManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredBrokerManifest): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredBrokerManifestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TieredBrokerManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredBrokerManifestEngine(flags: Registry): TieredBrokerManifestEngine {
    return TieredBrokerManifestEngine(
        steps = mutableListOf<TieredBrokerManifestModifier>(
            TieredTieredBrokerManifestAdjuster(
                mutableListOf<Double>(1505.0, 5569.0),
                mutableListOf<Double>(0.81, 0.92),
            ),
            TitleTieredBrokerManifestAdjuster(mutableMapOf<String, Double>("north" to 1.12, "west" to 1.13)),
            FlaggedTieredBrokerManifestAdjuster(flags, "tiered-broker-manifest-rolling", 2.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 6328.1,
    )
}
