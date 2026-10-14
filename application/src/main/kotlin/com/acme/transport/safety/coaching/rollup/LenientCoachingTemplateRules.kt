package com.acme.transport.safety.coaching.rollup

import com.acme.transport.kit.text.normalize

/** A rule a lenient coaching template must pass. Check returns its problems, or none. */
interface LenientCoachingTemplateRule {
    fun check(item: LenientCoachingTemplate): MutableList<String>
}

data class LenientCoachingTemplateReviewerLengthRule(var maxLength: Int) : LenientCoachingTemplateRule {
    override fun check(item: LenientCoachingTemplate): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCoachingTemplateAllowedCodeRule(
    var allowed: MutableList<String>,
) : LenientCoachingTemplateRule {
    override fun check(item: LenientCoachingTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class LenientCoachingTemplateExpiresAtRangeRule(var min: Int, var max: Int) : LenientCoachingTemplateRule {
    override fun check(item: LenientCoachingTemplate): MutableList<String> {
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

data class LenientCoachingTemplateRequiredReviewerRule(var message: String) : LenientCoachingTemplateRule {
    override fun check(item: LenientCoachingTemplate): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientCoachingTemplateCodePrefixRule(var prefix: String) : LenientCoachingTemplateRule {
    override fun check(item: LenientCoachingTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientCoachingTemplateRequiredLabelRule(var message: String) : LenientCoachingTemplateRule {
    override fun check(item: LenientCoachingTemplate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient coaching template. */
data class LenientCoachingTemplateValidator(var rules: MutableList<LenientCoachingTemplateRule>) {
    fun validate(item: LenientCoachingTemplate): MutableList<String> {
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

    fun requireValid(item: LenientCoachingTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientCoachingTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientCoachingTemplateValidator(): LenientCoachingTemplateValidator {
    return LenientCoachingTemplateValidator(
        mutableListOf<LenientCoachingTemplateRule>(
            LenientCoachingTemplateReviewerLengthRule(1000),
            LenientCoachingTemplateAllowedCodeRule(mutableListOf<String>("adaptive", "weighted", "weighted")),
            LenientCoachingTemplateExpiresAtRangeRule(64, 3600),
            LenientCoachingTemplateRequiredReviewerRule("lenient coaching template needs an entry"),
            LenientCoachingTemplateCodePrefixRule("X"),
            LenientCoachingTemplateRequiredLabelRule("lenient coaching template needs a value"),
        ),
    )
}
