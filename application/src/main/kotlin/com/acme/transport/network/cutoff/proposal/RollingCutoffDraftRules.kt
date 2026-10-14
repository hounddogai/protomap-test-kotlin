package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.text.normalize

/** A rule a rolling cutoff draft must pass. Check returns its problems, or none. */
interface RollingCutoffDraftRule {
    fun check(item: RollingCutoffDraft): MutableList<String>
}

data class RollingCutoffDraftCodePrefixRule(var prefix: String) : RollingCutoffDraftRule {
    override fun check(item: RollingCutoffDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffDraftDescriptionLengthRule(var maxLength: Int) : RollingCutoffDraftRule {
    override fun check(item: RollingCutoffDraft): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffDraftDiscountRangeRule(var min: Double, var max: Double) : RollingCutoffDraftRule {
    override fun check(item: RollingCutoffDraft): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class RollingCutoffDraftAllowedLocaleRule(var allowed: MutableList<String>) : RollingCutoffDraftRule {
    override fun check(item: RollingCutoffDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is too long")
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffDraftRequiredNameRule(var message: String) : RollingCutoffDraftRule {
    override fun check(item: RollingCutoffDraft): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffDraftNameLengthRule(var maxLength: Int) : RollingCutoffDraftRule {
    override fun check(item: RollingCutoffDraft): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling cutoff draft. */
data class RollingCutoffDraftValidator(var rules: MutableList<RollingCutoffDraftRule>) {
    fun validate(item: RollingCutoffDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingCutoffDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingCutoffDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingCutoffDraftValidator(): RollingCutoffDraftValidator {
    return RollingCutoffDraftValidator(
        mutableListOf<RollingCutoffDraftRule>(
            RollingCutoffDraftCodePrefixRule("Q"),
            RollingCutoffDraftDescriptionLengthRule(80),
            RollingCutoffDraftDiscountRangeRule(4.3, 106.0),
            RollingCutoffDraftAllowedLocaleRule(mutableListOf<String>("rolling")),
            RollingCutoffDraftRequiredNameRule("rolling cutoff draft needs a value"),
            RollingCutoffDraftNameLengthRule(16),
        ),
    )
}
