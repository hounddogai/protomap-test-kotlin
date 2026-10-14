package com.acme.transport.customs.declaration.plan

import com.acme.transport.kit.text.normalize

/** A rule a declaration plan must pass. Check returns its problems, or none. */
interface DeclarationPlanRule {
    fun check(item: DeclarationPlan): MutableList<String>
}

data class DeclarationPlanAllowedSourceRule(var allowed: MutableList<String>) : DeclarationPlanRule {
    override fun check(item: DeclarationPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is too long")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationPlanRetriesRangeRule(var min: Int, var max: Int) : DeclarationPlanRule {
    override fun check(item: DeclarationPlan): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class DeclarationPlanRequiredCodeRule(var message: String) : DeclarationPlanRule {
    override fun check(item: DeclarationPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationPlanRequiredSourceRule(var message: String) : DeclarationPlanRule {
    override fun check(item: DeclarationPlan): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationPlanPinnedGuardRule(var limit: Double) : DeclarationPlanRule {
    override fun check(item: DeclarationPlan): MutableList<String> {
        if (item.pinned && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationPlanLimitRangeRule(var min: Int, var max: Int) : DeclarationPlanRule {
    override fun check(item: DeclarationPlan): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a declaration plan. */
data class DeclarationPlanValidator(var rules: MutableList<DeclarationPlanRule>) {
    fun validate(item: DeclarationPlan): MutableList<String> {
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

    fun requireValid(item: DeclarationPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationPlanValidator(): DeclarationPlanValidator {
    return DeclarationPlanValidator(
        mutableListOf<DeclarationPlanRule>(
            DeclarationPlanAllowedSourceRule(mutableListOf<String>()),
            DeclarationPlanRetriesRangeRule(64, 64),
            DeclarationPlanRequiredCodeRule("declaration plan needs a value"),
            DeclarationPlanRequiredSourceRule("declaration plan needs this field"),
            DeclarationPlanPinnedGuardRule(8.4),
            DeclarationPlanLimitRangeRule(120, 255),
        ),
    )
}
