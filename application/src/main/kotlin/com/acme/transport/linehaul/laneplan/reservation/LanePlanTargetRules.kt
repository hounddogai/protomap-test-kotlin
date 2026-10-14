package com.acme.transport.linehaul.laneplan.reservation

import com.acme.transport.kit.text.normalize

/** A rule a lane plan target must pass. Check returns its problems, or none. */
interface LanePlanTargetRule {
    fun check(item: LanePlanTarget): MutableList<String>
}

data class LanePlanTargetBudgetRangeRule(var min: Double, var max: Double) : LanePlanTargetRule {
    override fun check(item: LanePlanTarget): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanTargetCodePrefixRule(var prefix: String) : LanePlanTargetRule {
    override fun check(item: LanePlanTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanTargetAllowedSummaryRule(var allowed: MutableList<String>) : LanePlanTargetRule {
    override fun check(item: LanePlanTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan target. */
data class LanePlanTargetValidator(var rules: MutableList<LanePlanTargetRule>) {
    fun validate(item: LanePlanTarget): MutableList<String> {
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

    fun requireValid(item: LanePlanTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanTargetValidator(): LanePlanTargetValidator {
    return LanePlanTargetValidator(
        mutableListOf<LanePlanTargetRule>(
            LanePlanTargetBudgetRangeRule(0.7, 1564.9),
            LanePlanTargetCodePrefixRule("X"),
            LanePlanTargetAllowedSummaryRule(mutableListOf<String>("fallback", "scheduled")),
        ),
    )
}
