package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tender record calculation. */
interface TenderRecordAdjuster {
    fun describe(): String
    fun adjust(item: TenderRecord, value: Double): Double
}

data class CountryTenderRecordAdjuster(var factors: MutableMap<String, Double>) : TenderRecordAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: TenderRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentTenderRecordAdjuster(var percent: Double) : TenderRecordAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TenderRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredTenderRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TenderRecordAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TenderRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tender record steps in order and keeps a trace of each. */
data class TenderRecordEngine(
    var steps: MutableList<TenderRecordAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TenderRecord): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TenderRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTenderRecordEngine(): TenderRecordEngine {
    return TenderRecordEngine(
        steps = mutableListOf<TenderRecordAdjuster>(
            CountryTenderRecordAdjuster(
                mutableMapOf<String, Double>("south" to 0.96, "retail" to 1.22, "north" to 0.95),
            ),
            PercentTenderRecordAdjuster(-0.6),
            TieredTenderRecordAdjuster(mutableListOf<Double>(3108.0, 9520.0), mutableListOf<Double>(1.12, 0.82)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 91432.0,
    )
}
