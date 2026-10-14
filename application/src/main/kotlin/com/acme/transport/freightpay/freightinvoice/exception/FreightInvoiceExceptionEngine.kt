package com.acme.transport.freightpay.freightinvoice.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the freight invoice exception calculation. */
interface FreightInvoiceExceptionAdjuster {
    fun describe(): String
    fun adjust(item: FreightInvoiceException, value: Double): Double
}

data class TieredFreightInvoiceExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FreightInvoiceExceptionAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FreightInvoiceException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumFreightInvoiceExceptionAdjuster(var minimum: Double) : FreightInvoiceExceptionAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FreightInvoiceException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FreightInvoiceExceptionCeilingOffsetAdjuster(var weight: Double) : FreightInvoiceExceptionAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: FreightInvoiceException, value: Double): Double = value + item.ceiling * weight
}

data class LaneFreightInvoiceExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : FreightInvoiceExceptionAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: FreightInvoiceException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedFreightInvoiceExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FreightInvoiceExceptionAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FreightInvoiceException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentFreightInvoiceExceptionAdjuster(var percent: Double) : FreightInvoiceExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FreightInvoiceException, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the freight invoice exception steps in order and keeps a trace of each. */
data class FreightInvoiceExceptionEngine(
    var steps: MutableList<FreightInvoiceExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FreightInvoiceException): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FreightInvoiceException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFreightInvoiceExceptionEngine(flags: Registry): FreightInvoiceExceptionEngine {
    return FreightInvoiceExceptionEngine(
        steps = mutableListOf<FreightInvoiceExceptionAdjuster>(
            TieredFreightInvoiceExceptionAdjuster(
                mutableListOf<Double>(1001.0, 1283.0, 2513.0, 6618.0),
                mutableListOf<Double>(1.08, 1.13, 0.96, 0.98),
            ),
            MinimumFreightInvoiceExceptionAdjuster(23.4),
            FreightInvoiceExceptionCeilingOffsetAdjuster(0.8),
            LaneFreightInvoiceExceptionAdjuster(mutableMapOf<String, Double>("north" to 0.73, "east" to 0.84)),
            FlaggedFreightInvoiceExceptionAdjuster(flags, "freight-invoice-exception-lenient", 0.4),
            PercentFreightInvoiceExceptionAdjuster(-3.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 59992.6,
    )
}
