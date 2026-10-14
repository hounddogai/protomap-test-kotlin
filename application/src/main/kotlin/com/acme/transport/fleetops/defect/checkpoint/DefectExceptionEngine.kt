package com.acme.transport.fleetops.defect.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the defect exception calculation. */
interface DefectExceptionFactor {
    fun describe(): String
    fun adjust(item: DefectException, value: Double): Double
}

data class UnitDefectExceptionAdjuster(var factors: MutableMap<String, Double>) : DefectExceptionFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: DefectException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentDefectExceptionAdjuster(var percent: Double) : DefectExceptionFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefectException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDefectExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefectExceptionFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefectException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredDefectExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefectExceptionFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefectException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapDefectExceptionAdjuster(var cap: Double) : DefectExceptionFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefectException, value: Double): Double = minOf(value, cap)
}

/** Runs the defect exception steps in order and keeps a trace of each. */
data class DefectExceptionEngine(
    var steps: MutableList<DefectExceptionFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefectException): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefectException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefectExceptionEngine(flags: Registry): DefectExceptionEngine {
    return DefectExceptionEngine(
        steps = mutableListOf<DefectExceptionFactor>(
            UnitDefectExceptionAdjuster(mutableMapOf<String, Double>("west" to 0.79)),
            PercentDefectExceptionAdjuster(1.0),
            FlaggedDefectExceptionAdjuster(flags, "defect-exception-tiered", 4.4),
            TieredDefectExceptionAdjuster(
                mutableListOf<Double>(1276.0, 5042.0),
                mutableListOf<Double>(1.19, 1.07),
            ),
            CapDefectExceptionAdjuster(11513.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 59999.1,
    )
}
