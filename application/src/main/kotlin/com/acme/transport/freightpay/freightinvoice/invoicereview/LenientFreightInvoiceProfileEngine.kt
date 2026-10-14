package com.acme.transport.freightpay.freightinvoice.invoicereview

import com.acme.transport.kit.flags.Registry

/** One step of the lenient freight invoice profile calculation. */
interface LenientFreightInvoiceProfileAdjuster {
    fun describe(): String
    fun adjust(item: LenientFreightInvoiceProfile, value: Double): Double
}

data class TieredLenientFreightInvoiceProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientFreightInvoiceProfileAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientFreightInvoiceProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLenientFreightInvoiceProfileAdjuster(
    var minimum: Double,
) : LenientFreightInvoiceProfileAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientFreightInvoiceProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLenientFreightInvoiceProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientFreightInvoiceProfileAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientFreightInvoiceProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lenient freight invoice profile steps in order and keeps a trace of each. */
data class LenientFreightInvoiceProfileEngine(
    var steps: MutableList<LenientFreightInvoiceProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientFreightInvoiceProfile): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientFreightInvoiceProfileAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LenientFreightInvoiceProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientFreightInvoiceProfileEngine(flags: Registry): LenientFreightInvoiceProfileEngine {
    return LenientFreightInvoiceProfileEngine(
        steps = mutableListOf<LenientFreightInvoiceProfileAdjuster>(
            TieredLenientFreightInvoiceProfileAdjuster(
                mutableListOf<Double>(1148.0, 2482.0, 3583.0, 4506.0),
                mutableListOf<Double>(0.91, 1.12, 1.13, 0.83),
            ),
            MinimumLenientFreightInvoiceProfileAdjuster(1.9),
            FlaggedLenientFreightInvoiceProfileAdjuster(flags, "lenient-freight-invoice-profile-global", -5.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 49942.3,
    )
}
