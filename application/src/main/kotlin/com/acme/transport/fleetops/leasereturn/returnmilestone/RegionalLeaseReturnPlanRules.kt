package com.acme.transport.fleetops.leasereturn.returnmilestone

import com.acme.transport.kit.text.normalize

/** A rule a regional lease return plan must pass. Check returns its problems, or none. */
interface RegionalLeaseReturnPlanRule {
    fun check(item: RegionalLeaseReturnPlan): MutableList<String>
}

data class RegionalLeaseReturnPlanCategoryLengthRule(var maxLength: Int) : RegionalLeaseReturnPlanRule {
    override fun check(item: RegionalLeaseReturnPlan): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnPlanAllowedTitleRule(
    var allowed: MutableList<String>,
) : RegionalLeaseReturnPlanRule {
    override fun check(item: RegionalLeaseReturnPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnPlanRequiredCurrencyRule(var message: String) : RegionalLeaseReturnPlanRule {
    override fun check(item: RegionalLeaseReturnPlan): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnPlanCodeLengthRule(var maxLength: Int) : RegionalLeaseReturnPlanRule {
    override fun check(item: RegionalLeaseReturnPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnPlanRequiredCodeRule(var message: String) : RegionalLeaseReturnPlanRule {
    override fun check(item: RegionalLeaseReturnPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnPlanCodePrefixRule(var prefix: String) : RegionalLeaseReturnPlanRule {
    override fun check(item: RegionalLeaseReturnPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional lease return plan. */
data class RegionalLeaseReturnPlanValidator(var rules: MutableList<RegionalLeaseReturnPlanRule>) {
    fun validate(item: RegionalLeaseReturnPlan): MutableList<String> {
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

    fun requireValid(item: RegionalLeaseReturnPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalLeaseReturnPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalLeaseReturnPlanValidator(): RegionalLeaseReturnPlanValidator {
    return RegionalLeaseReturnPlanValidator(
        mutableListOf<RegionalLeaseReturnPlanRule>(
            RegionalLeaseReturnPlanCategoryLengthRule(120),
            RegionalLeaseReturnPlanAllowedTitleRule(mutableListOf<String>("global")),
            RegionalLeaseReturnPlanRequiredCurrencyRule("regional lease return plan needs a value"),
            RegionalLeaseReturnPlanCodeLengthRule(32),
            RegionalLeaseReturnPlanRequiredCodeRule("regional lease return plan needs an entry"),
            RegionalLeaseReturnPlanCodePrefixRule("Q"),
        ),
    )
}
