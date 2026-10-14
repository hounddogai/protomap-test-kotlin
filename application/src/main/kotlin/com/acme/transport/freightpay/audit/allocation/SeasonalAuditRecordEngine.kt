package com.acme.transport.freightpay.audit.allocation

import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal audit record calculation. */
interface SeasonalAuditRecordFactor {
    fun describe(): String
    fun adjust(item: SeasonalAuditRecord, value: Double): Double
}

data class SeasonalAuditRecordFloorValueOffsetAdjuster(var weight: Double) : SeasonalAuditRecordFactor {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: SeasonalAuditRecord, value: Double): Double = value + item.floorValue * weight
}

data class MinimumSeasonalAuditRecordAdjuster(var minimum: Double) : SeasonalAuditRecordFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalAuditRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingSeasonalAuditRecordAdjuster(var step: Double) : SeasonalAuditRecordFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalAuditRecord, value: Double): Double = roundTo(value, step)
}

data class TitleSeasonalAuditRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalAuditRecordFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: SeasonalAuditRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredSeasonalAuditRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalAuditRecordFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalAuditRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the seasonal audit record steps in order and keeps a trace of each. */
data class SeasonalAuditRecordEngine(
    var steps: MutableList<SeasonalAuditRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalAuditRecord): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalAuditRecordFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalAuditRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalAuditRecordEngine(): SeasonalAuditRecordEngine {
    return SeasonalAuditRecordEngine(
        steps = mutableListOf<SeasonalAuditRecordFactor>(
            SeasonalAuditRecordFloorValueOffsetAdjuster(0.08),
            MinimumSeasonalAuditRecordAdjuster(43.9),
            RoundingSeasonalAuditRecordAdjuster(5.0),
            TitleSeasonalAuditRecordAdjuster(
                mutableMapOf<String, Double>("north" to 0.98, "east" to 0.83, "south" to 1.35),
            ),
            TieredSeasonalAuditRecordAdjuster(
                mutableListOf<Double>(8394.0, 8926.0, 9459.0, 9496.0),
                mutableListOf<Double>(1.1, 0.81, 0.93, 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 30794.8,
    )
}
