package com.acme.transport.fleetops.defect.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered defect manifest calculation. */
interface TieredDefectManifestStep {
    fun describe(): String
    fun adjust(item: TieredDefectManifest, value: Double): Double
}

data class CapTieredDefectManifestAdjuster(var cap: Double) : TieredDefectManifestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredDefectManifest, value: Double): Double = minOf(value, cap)
}

data class SegmentTieredDefectManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredDefectManifestStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: TieredDefectManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTieredDefectManifestAdjuster(var step: Double) : TieredDefectManifestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredDefectManifest, value: Double): Double = roundTo(value, step)
}

data class TieredDefectManifestLoadKgOffsetAdjuster(var weight: Double) : TieredDefectManifestStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: TieredDefectManifest, value: Double): Double = value + item.loadKg * weight
}

data class PercentTieredDefectManifestAdjuster(var percent: Double) : TieredDefectManifestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredDefectManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedTieredDefectManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredDefectManifestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredDefectManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tiered defect manifest steps in order and keeps a trace of each. */
data class TieredDefectManifestEngine(
    var steps: MutableList<TieredDefectManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredDefectManifest): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredDefectManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredDefectManifestEngine(flags: Registry): TieredDefectManifestEngine {
    return TieredDefectManifestEngine(
        steps = mutableListOf<TieredDefectManifestStep>(
            CapTieredDefectManifestAdjuster(18304.0),
            SegmentTieredDefectManifestAdjuster(mutableMapOf<String, Double>("east" to 0.99)),
            RoundingTieredDefectManifestAdjuster(0.05),
            TieredDefectManifestLoadKgOffsetAdjuster(1.34),
            PercentTieredDefectManifestAdjuster(-20.8),
            FlaggedTieredDefectManifestAdjuster(flags, "tiered-defect-manifest-manual", -8.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 41904.1,
    )
}
