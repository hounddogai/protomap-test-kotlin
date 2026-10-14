package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.kit.text.normalize

/** A rule a primary trailer yard plan must pass. Check returns its problems, or none. */
interface PrimaryTrailerYardPlanRule {
    fun check(item: PrimaryTrailerYardPlan): MutableList<String>
}

data class PrimaryTrailerYardPlanOwnerIdLengthRule(var maxLength: Int) : PrimaryTrailerYardPlanRule {
    override fun check(item: PrimaryTrailerYardPlan): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTrailerYardPlanRequiredOwnerIdRule(var message: String) : PrimaryTrailerYardPlanRule {
    override fun check(item: PrimaryTrailerYardPlan): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTrailerYardPlanAllowedCodeRule(
    var allowed: MutableList<String>,
) : PrimaryTrailerYardPlanRule {
    override fun check(item: PrimaryTrailerYardPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTrailerYardPlanExpiresAtRangeRule(var min: Int, var max: Int) : PrimaryTrailerYardPlanRule {
    override fun check(item: PrimaryTrailerYardPlan): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class PrimaryTrailerYardPlanRequiredCodeRule(var message: String) : PrimaryTrailerYardPlanRule {
    override fun check(item: PrimaryTrailerYardPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTrailerYardPlanCodeLengthRule(var maxLength: Int) : PrimaryTrailerYardPlanRule {
    override fun check(item: PrimaryTrailerYardPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary trailer yard plan. */
data class PrimaryTrailerYardPlanValidator(var rules: MutableList<PrimaryTrailerYardPlanRule>) {
    fun validate(item: PrimaryTrailerYardPlan): MutableList<String> {
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

    fun requireValid(item: PrimaryTrailerYardPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTrailerYardPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTrailerYardPlanValidator(): PrimaryTrailerYardPlanValidator {
    return PrimaryTrailerYardPlanValidator(
        mutableListOf<PrimaryTrailerYardPlanRule>(
            PrimaryTrailerYardPlanOwnerIdLengthRule(1000),
            PrimaryTrailerYardPlanRequiredOwnerIdRule("primary trailer yard plan needs a value"),
            PrimaryTrailerYardPlanAllowedCodeRule(mutableListOf<String>("strict")),
            PrimaryTrailerYardPlanExpiresAtRangeRule(120, 3600),
            PrimaryTrailerYardPlanRequiredCodeRule("primary trailer yard plan needs an entry"),
            PrimaryTrailerYardPlanCodeLengthRule(64),
        ),
    )
}
