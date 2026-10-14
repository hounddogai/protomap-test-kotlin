package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tender manifest calculation. */
interface TenderManifestFactor {
    fun describe(): String
    fun adjust(item: TenderManifest, value: Double): Double
}

data class FlaggedTenderManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TenderManifestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TenderManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTenderManifestAdjuster(var cap: Double) : TenderManifestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TenderManifest, value: Double): Double = minOf(value, cap)
}

data class PercentTenderManifestAdjuster(var percent: Double) : TenderManifestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TenderManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TenderManifestMaximumOffsetAdjuster(var weight: Double) : TenderManifestFactor {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: TenderManifest, value: Double): Double = value + item.maximum * weight
}

data class RoundingTenderManifestAdjuster(var step: Double) : TenderManifestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TenderManifest, value: Double): Double = roundTo(value, step)
}

data class NameTenderManifestAdjuster(var factors: MutableMap<String, Double>) : TenderManifestFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: TenderManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tender manifest steps in order and keeps a trace of each. */
data class TenderManifestEngine(
    var steps: MutableList<TenderManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TenderManifest): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TenderManifestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TenderManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTenderManifestEngine(flags: Registry): TenderManifestEngine {
    return TenderManifestEngine(
        steps = mutableListOf<TenderManifestFactor>(
            FlaggedTenderManifestAdjuster(flags, "tender-manifest-batched", -8.8),
            CapTenderManifestAdjuster(39204.0),
            PercentTenderManifestAdjuster(22.6),
            TenderManifestMaximumOffsetAdjuster(1.94),
            RoundingTenderManifestAdjuster(0.05),
            NameTenderManifestAdjuster(mutableMapOf<String, Double>("west" to 1.14)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 71437.8,
    )
}
