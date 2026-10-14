package com.acme.transport.freightpay.dispute.disputetemplate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient dispute template calculation. */
interface LenientDisputeTemplateFactor {
    fun describe(): String
    fun adjust(item: LenientDisputeTemplate, value: Double): Double
}

data class LenientDisputeTemplateExpiresAtOffsetAdjuster(var weight: Double) : LenientDisputeTemplateFactor {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: LenientDisputeTemplate, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class MinimumLenientDisputeTemplateAdjuster(var minimum: Double) : LenientDisputeTemplateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientDisputeTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLenientDisputeTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientDisputeTemplateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientDisputeTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleLenientDisputeTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientDisputeTemplateFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: LenientDisputeTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLenientDisputeTemplateAdjuster(var percent: Double) : LenientDisputeTemplateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientDisputeTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the lenient dispute template steps in order and keeps a trace of each. */
data class LenientDisputeTemplateEngine(
    var steps: MutableList<LenientDisputeTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientDisputeTemplate): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientDisputeTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientDisputeTemplateEngine(flags: Registry): LenientDisputeTemplateEngine {
    return LenientDisputeTemplateEngine(
        steps = mutableListOf<LenientDisputeTemplateFactor>(
            LenientDisputeTemplateExpiresAtOffsetAdjuster(0.69),
            MinimumLenientDisputeTemplateAdjuster(18.0),
            FlaggedLenientDisputeTemplateAdjuster(flags, "lenient-dispute-template-primary", 7.3),
            TitleLenientDisputeTemplateAdjuster(mutableMapOf<String, Double>("central" to 1.15)),
            PercentLenientDisputeTemplateAdjuster(6.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 79694.3,
    )
}
