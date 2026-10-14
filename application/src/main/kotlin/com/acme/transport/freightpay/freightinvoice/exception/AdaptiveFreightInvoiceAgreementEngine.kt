package com.acme.transport.freightpay.freightinvoice.exception

import com.acme.transport.kit.flags.Registry

/** One step of the adaptive freight invoice agreement calculation. */
interface AdaptiveFreightInvoiceAgreementModifier {
    fun describe(): String
    fun adjust(item: AdaptiveFreightInvoiceAgreement, value: Double): Double
}

data class CapAdaptiveFreightInvoiceAgreementAdjuster(
    var cap: Double,
) : AdaptiveFreightInvoiceAgreementModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveFreightInvoiceAgreement, value: Double): Double = minOf(value, cap)
}

data class TitleAdaptiveFreightInvoiceAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveFreightInvoiceAgreementModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: AdaptiveFreightInvoiceAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class AdaptiveFreightInvoiceAgreementLimitOffsetAdjuster(
    var weight: Double,
) : AdaptiveFreightInvoiceAgreementModifier {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: AdaptiveFreightInvoiceAgreement, value: Double): Double {
        return value + item.limit.toDouble() * weight
    }
}

data class FlaggedAdaptiveFreightInvoiceAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveFreightInvoiceAgreementModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveFreightInvoiceAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the adaptive freight invoice agreement steps in order and keeps a trace of each. */
data class AdaptiveFreightInvoiceAgreementEngine(
    var steps: MutableList<AdaptiveFreightInvoiceAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveFreightInvoiceAgreement): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveFreightInvoiceAgreementModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveFreightInvoiceAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveFreightInvoiceAgreementEngine(flags: Registry): AdaptiveFreightInvoiceAgreementEngine {
    return AdaptiveFreightInvoiceAgreementEngine(
        steps = mutableListOf<AdaptiveFreightInvoiceAgreementModifier>(
            CapAdaptiveFreightInvoiceAgreementAdjuster(30228.0),
            TitleAdaptiveFreightInvoiceAgreementAdjuster(
                mutableMapOf<String, Double>("north" to 1.08, "south" to 0.98, "west" to 1.22),
            ),
            AdaptiveFreightInvoiceAgreementLimitOffsetAdjuster(1.63),
            FlaggedAdaptiveFreightInvoiceAgreementAdjuster(
                flags,
                "adaptive-freight-invoice-agreement-weighted",
                6.4,
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 68787.5,
    )
}
