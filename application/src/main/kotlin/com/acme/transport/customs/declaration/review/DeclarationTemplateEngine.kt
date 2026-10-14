package com.acme.transport.customs.declaration.review

import com.acme.transport.kit.flags.Registry

/** One step of the declaration template calculation. */
interface DeclarationTemplateStep {
    fun describe(): String
    fun adjust(item: DeclarationTemplate, value: Double): Double
}

data class SourceDeclarationTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : DeclarationTemplateStep {
    override fun describe(): String = "by source"

    override fun adjust(item: DeclarationTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDeclarationTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DeclarationTemplateStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DeclarationTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDeclarationTemplateAdjuster(var percent: Double) : DeclarationTemplateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DeclarationTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDeclarationTemplateAdjuster(var cap: Double) : DeclarationTemplateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DeclarationTemplate, value: Double): Double = minOf(value, cap)
}

/** Runs the declaration template steps in order and keeps a trace of each. */
data class DeclarationTemplateEngine(
    var steps: MutableList<DeclarationTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DeclarationTemplate): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DeclarationTemplateStep = steps[index]
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

    fun evaluateAll(items: MutableList<DeclarationTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDeclarationTemplateEngine(flags: Registry): DeclarationTemplateEngine {
    return DeclarationTemplateEngine(
        steps = mutableListOf<DeclarationTemplateStep>(
            SourceDeclarationTemplateAdjuster(mutableMapOf<String, Double>("west" to 1.05)),
            FlaggedDeclarationTemplateAdjuster(flags, "declaration-template-fallback", 5.1),
            PercentDeclarationTemplateAdjuster(-8.1),
            CapDeclarationTemplateAdjuster(8363.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 80815.8,
    )
}
