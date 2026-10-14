package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a strict attempt template must pass. Check returns its problems, or none. */
interface StrictAttemptTemplateRule {
    fun check(item: StrictAttemptTemplate): MutableList<String>
}

data class StrictAttemptTemplateLocaleLengthRule(var maxLength: Int) : StrictAttemptTemplateRule {
    override fun check(item: StrictAttemptTemplate): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictAttemptTemplateRequiredTitleRule(var message: String) : StrictAttemptTemplateRule {
    override fun check(item: StrictAttemptTemplate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictAttemptTemplateRequiredCurrencyRule(var message: String) : StrictAttemptTemplateRule {
    override fun check(item: StrictAttemptTemplate): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictAttemptTemplateAllowedCodeRule(var allowed: MutableList<String>) : StrictAttemptTemplateRule {
    override fun check(item: StrictAttemptTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class StrictAttemptTemplateCurrencyLengthRule(var maxLength: Int) : StrictAttemptTemplateRule {
    override fun check(item: StrictAttemptTemplate): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictAttemptTemplateScoreRangeRule(var min: Double, var max: Double) : StrictAttemptTemplateRule {
    override fun check(item: StrictAttemptTemplate): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict attempt template. */
data class StrictAttemptTemplateValidator(var rules: MutableList<StrictAttemptTemplateRule>) {
    fun validate(item: StrictAttemptTemplate): MutableList<String> {
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

    fun requireValid(item: StrictAttemptTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictAttemptTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictAttemptTemplateValidator(): StrictAttemptTemplateValidator {
    return StrictAttemptTemplateValidator(
        mutableListOf<StrictAttemptTemplateRule>(
            StrictAttemptTemplateLocaleLengthRule(1000),
            StrictAttemptTemplateRequiredTitleRule("strict attempt template needs this field"),
            StrictAttemptTemplateRequiredCurrencyRule("strict attempt template needs this field"),
            StrictAttemptTemplateAllowedCodeRule(mutableListOf<String>("fallback")),
            StrictAttemptTemplateCurrencyLengthRule(120),
            StrictAttemptTemplateScoreRangeRule(9.5, 3507.4),
        ),
    )
}
