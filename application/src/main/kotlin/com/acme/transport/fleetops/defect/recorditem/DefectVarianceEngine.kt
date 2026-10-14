package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the defect variance calculation. */
interface DefectVarianceFactor {
    fun describe(): String
    fun adjust(item: DefectVariance, value: Double): Double
}

data class CapDefectVarianceAdjuster(var cap: Double) : DefectVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefectVariance, value: Double): Double = minOf(value, cap)
}

data class MinimumDefectVarianceAdjuster(var minimum: Double) : DefectVarianceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefectVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CategoryDefectVarianceAdjuster(var factors: MutableMap<String, Double>) : DefectVarianceFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: DefectVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDefectVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefectVarianceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefectVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the defect variance steps in order and keeps a trace of each. */
data class DefectVarianceEngine(
    var steps: MutableList<DefectVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefectVariance): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefectVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefectVarianceEngine(flags: Registry): DefectVarianceEngine {
    return DefectVarianceEngine(
        steps = mutableListOf<DefectVarianceFactor>(
            CapDefectVarianceAdjuster(37762.0),
            MinimumDefectVarianceAdjuster(14.1),
            CategoryDefectVarianceAdjuster(
                mutableMapOf<String, Double>("east" to 1.17, "online" to 1.2, "west" to 1.4),
            ),
            FlaggedDefectVarianceAdjuster(flags, "defect-variance-batched", 5.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 61312.2,
    )
}
