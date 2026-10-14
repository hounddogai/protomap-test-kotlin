package com.acme.transport.customs.declaration.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the declaration profile calculation. */
interface DeclarationProfileAdjuster {
    fun describe(): String
    fun adjust(item: DeclarationProfile, value: Double): Double
}

data class TitleDeclarationProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : DeclarationProfileAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: DeclarationProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDeclarationProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DeclarationProfileAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DeclarationProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredDeclarationProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationProfileAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapDeclarationProfileAdjuster(var cap: Double) : DeclarationProfileAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DeclarationProfile, value: Double): Double = minOf(value, cap)
}

/** Runs the declaration profile steps in order and keeps a trace of each. */
data class DeclarationProfileEngine(
    var steps: MutableList<DeclarationProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DeclarationProfile): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DeclarationProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDeclarationProfileEngine(flags: Registry): DeclarationProfileEngine {
    return DeclarationProfileEngine(
        steps = mutableListOf<DeclarationProfileAdjuster>(
            TitleDeclarationProfileAdjuster(
                mutableMapOf<String, Double>("north" to 1.17, "online" to 1.24, "retail" to 1.21),
            ),
            FlaggedDeclarationProfileAdjuster(flags, "declaration-profile-seasonal", -6.8),
            TieredDeclarationProfileAdjuster(
                mutableListOf<Double>(8751.0, 9298.0),
                mutableListOf<Double>(0.93, 1.19),
            ),
            CapDeclarationProfileAdjuster(18561.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 43780.2,
    )
}
