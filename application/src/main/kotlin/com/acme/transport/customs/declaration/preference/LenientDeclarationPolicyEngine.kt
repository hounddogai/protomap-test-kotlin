package com.acme.transport.customs.declaration.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient declaration policy calculation. */
interface LenientDeclarationPolicyModifier {
    fun describe(): String
    fun adjust(item: LenientDeclarationPolicy, value: Double): Double
}

data class TieredLenientDeclarationPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientDeclarationPolicyModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientDeclarationPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLenientDeclarationPolicyAdjuster(var percent: Double) : LenientDeclarationPolicyModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientDeclarationPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LenientDeclarationPolicyEndsAtOffsetAdjuster(var weight: Double) : LenientDeclarationPolicyModifier {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: LenientDeclarationPolicy, value: Double): Double {
        return value + item.endsAt.toDouble() * weight
    }
}

data class FlaggedLenientDeclarationPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientDeclarationPolicyModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientDeclarationPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLenientDeclarationPolicyAdjuster(var cap: Double) : LenientDeclarationPolicyModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientDeclarationPolicy, value: Double): Double = minOf(value, cap)
}

data class NameLenientDeclarationPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientDeclarationPolicyModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: LenientDeclarationPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient declaration policy steps in order and keeps a trace of each. */
data class LenientDeclarationPolicyEngine(
    var steps: MutableList<LenientDeclarationPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientDeclarationPolicy): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientDeclarationPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientDeclarationPolicyEngine(flags: Registry): LenientDeclarationPolicyEngine {
    return LenientDeclarationPolicyEngine(
        steps = mutableListOf<LenientDeclarationPolicyModifier>(
            TieredLenientDeclarationPolicyAdjuster(
                mutableListOf<Double>(5733.0, 8668.0),
                mutableListOf<Double>(1.19, 1.05),
            ),
            PercentLenientDeclarationPolicyAdjuster(23.1),
            LenientDeclarationPolicyEndsAtOffsetAdjuster(0.83),
            FlaggedLenientDeclarationPolicyAdjuster(flags, "lenient-declaration-policy-pending", 9.0),
            CapLenientDeclarationPolicyAdjuster(15425.0),
            NameLenientDeclarationPolicyAdjuster(
                mutableMapOf<String, Double>("north" to 0.79, "retail" to 1.2, "east" to 0.8),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 76876.1,
    )
}
