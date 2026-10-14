package com.acme.transport.linehaul.trailer.budget

import com.acme.transport.kit.text.normalize

/** A rule a fallback trailer override must pass. Check returns its problems, or none. */
interface FallbackTrailerOverrideRule {
    fun check(item: FallbackTrailerOverride): MutableList<String>
}

data class FallbackTrailerOverrideQuantityRangeRule(var min: Int, var max: Int) : FallbackTrailerOverrideRule {
    override fun check(item: FallbackTrailerOverride): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTrailerOverrideCodeLengthRule(var maxLength: Int) : FallbackTrailerOverrideRule {
    override fun check(item: FallbackTrailerOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTrailerOverrideRequiredCategoryRule(var message: String) : FallbackTrailerOverrideRule {
    override fun check(item: FallbackTrailerOverride): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTrailerOverrideAllowedCategoryRule(
    var allowed: MutableList<String>,
) : FallbackTrailerOverrideRule {
    override fun check(item: FallbackTrailerOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTrailerOverrideRequiredLabelRule(var message: String) : FallbackTrailerOverrideRule {
    override fun check(item: FallbackTrailerOverride): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTrailerOverrideCategoryLengthRule(var maxLength: Int) : FallbackTrailerOverrideRule {
    override fun check(item: FallbackTrailerOverride): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback trailer override. */
data class FallbackTrailerOverrideValidator(var rules: MutableList<FallbackTrailerOverrideRule>) {
    fun validate(item: FallbackTrailerOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackTrailerOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackTrailerOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackTrailerOverrideValidator(): FallbackTrailerOverrideValidator {
    return FallbackTrailerOverrideValidator(
        mutableListOf<FallbackTrailerOverrideRule>(
            FallbackTrailerOverrideQuantityRangeRule(255, 3600),
            FallbackTrailerOverrideCodeLengthRule(3600),
            FallbackTrailerOverrideRequiredCategoryRule("fallback trailer override needs this field"),
            FallbackTrailerOverrideAllowedCategoryRule(mutableListOf<String>()),
            FallbackTrailerOverrideRequiredLabelRule("fallback trailer override needs this field"),
            FallbackTrailerOverrideCategoryLengthRule(32),
        ),
    )
}
