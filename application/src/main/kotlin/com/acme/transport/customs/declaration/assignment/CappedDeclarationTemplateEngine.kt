package com.acme.transport.customs.declaration.assignment

/** One step of the capped declaration template calculation. */
interface CappedDeclarationTemplateModifier {
    fun describe(): String
    fun adjust(item: CappedDeclarationTemplate, value: Double): Double
}

data class MinimumCappedDeclarationTemplateAdjuster(var minimum: Double) : CappedDeclarationTemplateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedDeclarationTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapCappedDeclarationTemplateAdjuster(var cap: Double) : CappedDeclarationTemplateModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedDeclarationTemplate, value: Double): Double = minOf(value, cap)
}

data class CappedDeclarationTemplatePalletsOffsetAdjuster(
    var weight: Double,
) : CappedDeclarationTemplateModifier {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: CappedDeclarationTemplate, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class PercentCappedDeclarationTemplateAdjuster(var percent: Double) : CappedDeclarationTemplateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedDeclarationTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capped declaration template steps in order and keeps a trace of each. */
data class CappedDeclarationTemplateEngine(
    var steps: MutableList<CappedDeclarationTemplateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedDeclarationTemplate): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedDeclarationTemplateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CappedDeclarationTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedDeclarationTemplateEngine(): CappedDeclarationTemplateEngine {
    return CappedDeclarationTemplateEngine(
        steps = mutableListOf<CappedDeclarationTemplateModifier>(
            MinimumCappedDeclarationTemplateAdjuster(22.6),
            CapCappedDeclarationTemplateAdjuster(41560.0),
            CappedDeclarationTemplatePalletsOffsetAdjuster(1.52),
            PercentCappedDeclarationTemplateAdjuster(21.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 32983.7,
    )
}
