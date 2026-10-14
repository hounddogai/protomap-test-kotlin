package com.acme.transport.network.node.policy

import com.acme.transport.kit.text.normalize

/** A rule a seasonal node plan must pass. Check returns its problems, or none. */
interface SeasonalNodePlanRule {
    fun check(item: SeasonalNodePlan): MutableList<String>
}

data class SeasonalNodePlanRequiredLabelRule(var message: String) : SeasonalNodePlanRule {
    override fun check(item: SeasonalNodePlan): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalNodePlanCodeLengthRule(var maxLength: Int) : SeasonalNodePlanRule {
    override fun check(item: SeasonalNodePlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalNodePlanFuelLitersRangeRule(var min: Double, var max: Double) : SeasonalNodePlanRule {
    override fun check(item: SeasonalNodePlan): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class SeasonalNodePlanAllowedCodeRule(var allowed: MutableList<String>) : SeasonalNodePlanRule {
    override fun check(item: SeasonalNodePlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalNodePlanRequiredCodeRule(var message: String) : SeasonalNodePlanRule {
    override fun check(item: SeasonalNodePlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal node plan. */
data class SeasonalNodePlanValidator(var rules: MutableList<SeasonalNodePlanRule>) {
    fun validate(item: SeasonalNodePlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalNodePlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalNodePlan>): MutableMap<String, MutableList<String>> {
        val report = mutableMapOf<String, MutableList<String>>()
        for (item in items) {
            val problems = validate(item)
            if (problems.size > 0) {
                report[item.code] = problems
            }
        }
        return report
    }
}

fun defaultSeasonalNodePlanValidator(): SeasonalNodePlanValidator {
    return SeasonalNodePlanValidator(
        mutableListOf<SeasonalNodePlanRule>(
            SeasonalNodePlanRequiredLabelRule("seasonal node plan needs an entry"),
            SeasonalNodePlanCodeLengthRule(86400),
            SeasonalNodePlanFuelLitersRangeRule(1.0, 502.4),
            SeasonalNodePlanAllowedCodeRule(mutableListOf<String>("regional", "regional", "strict")),
            SeasonalNodePlanRequiredCodeRule("seasonal node plan needs this field"),
        ),
    )
}
