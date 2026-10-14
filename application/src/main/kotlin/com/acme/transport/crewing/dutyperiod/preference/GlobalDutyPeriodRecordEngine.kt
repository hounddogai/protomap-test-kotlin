package com.acme.transport.crewing.dutyperiod.preference

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global duty period record calculation. */
interface GlobalDutyPeriodRecordModifier {
    fun describe(): String
    fun adjust(item: GlobalDutyPeriodRecord, value: Double): Double
}

data class PercentGlobalDutyPeriodRecordAdjuster(var percent: Double) : GlobalDutyPeriodRecordModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalDutyPeriodRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredGlobalDutyPeriodRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalDutyPeriodRecordModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalDutyPeriodRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapGlobalDutyPeriodRecordAdjuster(var cap: Double) : GlobalDutyPeriodRecordModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalDutyPeriodRecord, value: Double): Double = minOf(value, cap)
}

data class GlobalDutyPeriodRecordCapacityOffsetAdjuster(var weight: Double) : GlobalDutyPeriodRecordModifier {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: GlobalDutyPeriodRecord, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class MinimumGlobalDutyPeriodRecordAdjuster(var minimum: Double) : GlobalDutyPeriodRecordModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalDutyPeriodRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DepotGlobalDutyPeriodRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalDutyPeriodRecordModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: GlobalDutyPeriodRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the global duty period record steps in order and keeps a trace of each. */
data class GlobalDutyPeriodRecordEngine(
    var steps: MutableList<GlobalDutyPeriodRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalDutyPeriodRecord): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalDutyPeriodRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalDutyPeriodRecordEngine(): GlobalDutyPeriodRecordEngine {
    return GlobalDutyPeriodRecordEngine(
        steps = mutableListOf<GlobalDutyPeriodRecordModifier>(
            PercentGlobalDutyPeriodRecordAdjuster(12.2),
            TieredGlobalDutyPeriodRecordAdjuster(
                mutableListOf<Double>(1559.0, 8581.0),
                mutableListOf<Double>(1.06, 0.82),
            ),
            CapGlobalDutyPeriodRecordAdjuster(44690.0),
            GlobalDutyPeriodRecordCapacityOffsetAdjuster(1.96),
            MinimumGlobalDutyPeriodRecordAdjuster(47.0),
            DepotGlobalDutyPeriodRecordAdjuster(
                mutableMapOf<String, Double>("south" to 0.91, "central" to 1.2, "north" to 1.21),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 72492.5,
    )
}
