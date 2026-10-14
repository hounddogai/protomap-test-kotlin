package com.acme.transport.fleetops.leasereturn.returntier

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient lease return manifest calculation. */
interface LenientLeaseReturnManifestModifier {
    fun describe(): String
    fun adjust(item: LenientLeaseReturnManifest, value: Double): Double
}

data class CapLenientLeaseReturnManifestAdjuster(var cap: Double) : LenientLeaseReturnManifestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientLeaseReturnManifest, value: Double): Double = minOf(value, cap)
}

data class TitleLenientLeaseReturnManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientLeaseReturnManifestModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: LenientLeaseReturnManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLenientLeaseReturnManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientLeaseReturnManifestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientLeaseReturnManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLenientLeaseReturnManifestAdjuster(var step: Double) : LenientLeaseReturnManifestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientLeaseReturnManifest, value: Double): Double = roundTo(value, step)
}

data class PercentLenientLeaseReturnManifestAdjuster(var percent: Double) : LenientLeaseReturnManifestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientLeaseReturnManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the lenient lease return manifest steps in order and keeps a trace of each. */
data class LenientLeaseReturnManifestEngine(
    var steps: MutableList<LenientLeaseReturnManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientLeaseReturnManifest): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientLeaseReturnManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientLeaseReturnManifestEngine(): LenientLeaseReturnManifestEngine {
    return LenientLeaseReturnManifestEngine(
        steps = mutableListOf<LenientLeaseReturnManifestModifier>(
            CapLenientLeaseReturnManifestAdjuster(6959.0),
            TitleLenientLeaseReturnManifestAdjuster(
                mutableMapOf<String, Double>("north" to 0.78, "west" to 0.88, "south" to 1.24),
            ),
            TieredLenientLeaseReturnManifestAdjuster(
                mutableListOf<Double>(3718.0, 5392.0, 9420.0, 9872.0),
                mutableListOf<Double>(1.05, 0.94, 0.86, 0.82),
            ),
            RoundingLenientLeaseReturnManifestAdjuster(0.5),
            PercentLenientLeaseReturnManifestAdjuster(9.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 90836.3,
    )
}
