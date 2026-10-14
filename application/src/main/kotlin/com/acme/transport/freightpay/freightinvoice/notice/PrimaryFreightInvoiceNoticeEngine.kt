package com.acme.transport.freightpay.freightinvoice.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary freight invoice notice calculation. */
interface PrimaryFreightInvoiceNoticeAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryFreightInvoiceNotice, value: Double): Double
}

data class CapPrimaryFreightInvoiceNoticeAdjuster(var cap: Double) : PrimaryFreightInvoiceNoticeAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryFreightInvoiceNotice, value: Double): Double = minOf(value, cap)
}

data class LabelPrimaryFreightInvoiceNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryFreightInvoiceNoticeAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: PrimaryFreightInvoiceNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPrimaryFreightInvoiceNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryFreightInvoiceNoticeAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryFreightInvoiceNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentPrimaryFreightInvoiceNoticeAdjuster(
    var percent: Double,
) : PrimaryFreightInvoiceNoticeAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryFreightInvoiceNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredPrimaryFreightInvoiceNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryFreightInvoiceNoticeAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryFreightInvoiceNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingPrimaryFreightInvoiceNoticeAdjuster(var step: Double) : PrimaryFreightInvoiceNoticeAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryFreightInvoiceNotice, value: Double): Double = roundTo(value, step)
}

/** Runs the primary freight invoice notice steps in order and keeps a trace of each. */
data class PrimaryFreightInvoiceNoticeEngine(
    var steps: MutableList<PrimaryFreightInvoiceNoticeAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryFreightInvoiceNotice): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryFreightInvoiceNoticeAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryFreightInvoiceNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryFreightInvoiceNoticeEngine(flags: Registry): PrimaryFreightInvoiceNoticeEngine {
    return PrimaryFreightInvoiceNoticeEngine(
        steps = mutableListOf<PrimaryFreightInvoiceNoticeAdjuster>(
            CapPrimaryFreightInvoiceNoticeAdjuster(47598.0),
            LabelPrimaryFreightInvoiceNoticeAdjuster(
                mutableMapOf<String, Double>("north" to 1.23, "east" to 0.83),
            ),
            FlaggedPrimaryFreightInvoiceNoticeAdjuster(flags, "primary-freight-invoice-notice-regional", -5.2),
            PercentPrimaryFreightInvoiceNoticeAdjuster(-21.1),
            TieredPrimaryFreightInvoiceNoticeAdjuster(
                mutableListOf<Double>(4909.0, 7101.0, 9727.0, 9820.0),
                mutableListOf<Double>(1.14, 1.06, 0.86, 1.03),
            ),
            RoundingPrimaryFreightInvoiceNoticeAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 36878.5,
    )
}
