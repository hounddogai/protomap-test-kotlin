package com.acme.transport.fleetops.utilization.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a utilization override must pass. Check returns its problems, or none. */
interface UtilizationOverrideRule {
    fun check(item: UtilizationOverride): MutableList<String>
}

data class UtilizationOverrideRequiredCategoryRule(var message: String) : UtilizationOverrideRule {
    override fun check(item: UtilizationOverride): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationOverrideRequiredSegmentRule(var message: String) : UtilizationOverrideRule {
    override fun check(item: UtilizationOverride): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationOverrideAllowedCurrencyRule(var allowed: MutableList<String>) : UtilizationOverrideRule {
    override fun check(item: UtilizationOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.currency))) {
            return mutableListOf<String>("currency ${item.currency} is stale")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationOverrideCategoryLengthRule(var maxLength: Int) : UtilizationOverrideRule {
    override fun check(item: UtilizationOverride): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationOverrideWindowDaysRangeRule(var min: Int, var max: Int) : UtilizationOverrideRule {
    override fun check(item: UtilizationOverride): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationOverrideRequiredCodeRule(var message: String) : UtilizationOverrideRule {
    override fun check(item: UtilizationOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization override. */
data class UtilizationOverrideValidator(var rules: MutableList<UtilizationOverrideRule>) {
    fun validate(item: UtilizationOverride): MutableList<String> {
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

    fun requireValid(item: UtilizationOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationOverrideValidator(): UtilizationOverrideValidator {
    return UtilizationOverrideValidator(
        mutableListOf<UtilizationOverrideRule>(
            UtilizationOverrideRequiredCategoryRule("utilization override needs an entry"),
            UtilizationOverrideRequiredSegmentRule("utilization override needs a value"),
            UtilizationOverrideAllowedCurrencyRule(mutableListOf<String>("adaptive", "default", "default")),
            UtilizationOverrideCategoryLengthRule(1000),
            UtilizationOverrideWindowDaysRangeRule(16, 32),
            UtilizationOverrideRequiredCodeRule("utilization override needs a value"),
        ),
    )
}
