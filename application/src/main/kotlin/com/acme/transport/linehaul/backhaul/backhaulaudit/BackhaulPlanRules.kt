package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.kit.text.normalize

/** A rule a backhaul plan must pass. Check returns its problems, or none. */
interface BackhaulPlanRule {
    fun check(item: BackhaulPlan): MutableList<String>
}

data class BackhaulPlanAllowedDescriptionRule(var allowed: MutableList<String>) : BackhaulPlanRule {
    override fun check(item: BackhaulPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is stale")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulPlanCodePrefixRule(var prefix: String) : BackhaulPlanRule {
    override fun check(item: BackhaulPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulPlanCodeLengthRule(var maxLength: Int) : BackhaulPlanRule {
    override fun check(item: BackhaulPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulPlanRequiredCodeRule(var message: String) : BackhaulPlanRule {
    override fun check(item: BackhaulPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulPlanBudgetRangeRule(var min: Double, var max: Double) : BackhaulPlanRule {
    override fun check(item: BackhaulPlan): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class BackhaulPlanDockLengthRule(var maxLength: Int) : BackhaulPlanRule {
    override fun check(item: BackhaulPlan): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul plan. */
data class BackhaulPlanValidator(var rules: MutableList<BackhaulPlanRule>) {
    fun validate(item: BackhaulPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BackhaulPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulPlanValidator(): BackhaulPlanValidator {
    return BackhaulPlanValidator(
        mutableListOf<BackhaulPlanRule>(
            BackhaulPlanAllowedDescriptionRule(mutableListOf<String>()),
            BackhaulPlanCodePrefixRule("ACM"),
            BackhaulPlanCodeLengthRule(64),
            BackhaulPlanRequiredCodeRule("backhaul plan needs an entry"),
            BackhaulPlanBudgetRangeRule(0.2, 1681.9),
            BackhaulPlanDockLengthRule(86400),
        ),
    )
}
