package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient node manifest calculation. */
interface LenientNodeManifestAdjuster {
    fun describe(): String
    fun adjust(item: LenientNodeManifest, value: Double): Double
}

data class RoundingLenientNodeManifestAdjuster(var step: Double) : LenientNodeManifestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientNodeManifest, value: Double): Double = roundTo(value, step)
}

data class NameLenientNodeManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientNodeManifestAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: LenientNodeManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLenientNodeManifestAdjuster(var cap: Double) : LenientNodeManifestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientNodeManifest, value: Double): Double = minOf(value, cap)
}

data class MinimumLenientNodeManifestAdjuster(var minimum: Double) : LenientNodeManifestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientNodeManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLenientNodeManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientNodeManifestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientNodeManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lenient node manifest steps in order and keeps a trace of each. */
data class LenientNodeManifestEngine(
    var steps: MutableList<LenientNodeManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientNodeManifest): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientNodeManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientNodeManifestEngine(flags: Registry): LenientNodeManifestEngine {
    return LenientNodeManifestEngine(
        steps = mutableListOf<LenientNodeManifestAdjuster>(
            RoundingLenientNodeManifestAdjuster(0.05),
            NameLenientNodeManifestAdjuster(mutableMapOf<String, Double>("north" to 1.0, "west" to 0.98)),
            CapLenientNodeManifestAdjuster(20396.0),
            MinimumLenientNodeManifestAdjuster(6.1),
            FlaggedLenientNodeManifestAdjuster(flags, "lenient-node-manifest-weighted", 3.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 66661.3,
    )
}
