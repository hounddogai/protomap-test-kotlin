package com.acme.transport.terminal.traileryard.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped trailer yard manifest calculation. */
interface CappedTrailerYardManifestFactor {
    fun describe(): String
    fun adjust(item: CappedTrailerYardManifest, value: Double): Double
}

data class CappedTrailerYardManifestStartsAtOffsetAdjuster(
    var weight: Double,
) : CappedTrailerYardManifestFactor {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: CappedTrailerYardManifest, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class FlaggedCappedTrailerYardManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedTrailerYardManifestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedTrailerYardManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CategoryCappedTrailerYardManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedTrailerYardManifestFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: CappedTrailerYardManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped trailer yard manifest steps in order and keeps a trace of each. */
data class CappedTrailerYardManifestEngine(
    var steps: MutableList<CappedTrailerYardManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedTrailerYardManifest): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedTrailerYardManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedTrailerYardManifestEngine(flags: Registry): CappedTrailerYardManifestEngine {
    return CappedTrailerYardManifestEngine(
        steps = mutableListOf<CappedTrailerYardManifestFactor>(
            CappedTrailerYardManifestStartsAtOffsetAdjuster(0.94),
            FlaggedCappedTrailerYardManifestAdjuster(flags, "capped-trailer-yard-manifest-primary", 9.8),
            CategoryCappedTrailerYardManifestAdjuster(
                mutableMapOf<String, Double>("south" to 1.29, "west" to 0.74),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 22365.4,
    )
}
