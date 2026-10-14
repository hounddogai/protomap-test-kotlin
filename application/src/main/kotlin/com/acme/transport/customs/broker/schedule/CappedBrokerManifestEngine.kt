package com.acme.transport.customs.broker.schedule

import com.acme.transport.kit.flags.Registry

/** One step of the capped broker manifest calculation. */
interface CappedBrokerManifestStep {
    fun describe(): String
    fun adjust(item: CappedBrokerManifest, value: Double): Double
}

data class CapCappedBrokerManifestAdjuster(var cap: Double) : CappedBrokerManifestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedBrokerManifest, value: Double): Double = minOf(value, cap)
}

data class CappedBrokerManifestWeightOffsetAdjuster(var weight: Double) : CappedBrokerManifestStep {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: CappedBrokerManifest, value: Double): Double = value + item.weight * weight
}

data class CategoryCappedBrokerManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedBrokerManifestStep {
    override fun describe(): String = "by category"

    override fun adjust(item: CappedBrokerManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCappedBrokerManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedBrokerManifestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedBrokerManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped broker manifest steps in order and keeps a trace of each. */
data class CappedBrokerManifestEngine(
    var steps: MutableList<CappedBrokerManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedBrokerManifest): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedBrokerManifestStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedBrokerManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedBrokerManifestEngine(flags: Registry): CappedBrokerManifestEngine {
    return CappedBrokerManifestEngine(
        steps = mutableListOf<CappedBrokerManifestStep>(
            CapCappedBrokerManifestAdjuster(33196.0),
            CappedBrokerManifestWeightOffsetAdjuster(1.3),
            CategoryCappedBrokerManifestAdjuster(
                mutableMapOf<String, Double>("east" to 0.92, "retail" to 0.88, "south" to 1.15),
            ),
            FlaggedCappedBrokerManifestAdjuster(flags, "capped-broker-manifest-lenient", 9.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 77626.7,
    )
}
