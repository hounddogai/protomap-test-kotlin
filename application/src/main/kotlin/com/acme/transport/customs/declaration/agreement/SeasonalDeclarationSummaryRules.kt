package com.acme.transport.customs.declaration.agreement

import com.acme.transport.kit.text.normalize

/** A rule a seasonal declaration summary must pass. Check returns its problems, or none. */
interface SeasonalDeclarationSummaryRule {
    fun check(item: SeasonalDeclarationSummary): MutableList<String>
}

data class SeasonalDeclarationSummaryRequiredTitleRule(var message: String) : SeasonalDeclarationSummaryRule {
    override fun check(item: SeasonalDeclarationSummary): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDeclarationSummaryAllowedCodeRule(
    var allowed: MutableList<String>,
) : SeasonalDeclarationSummaryRule {
    override fun check(item: SeasonalDeclarationSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDeclarationSummaryQuantityRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalDeclarationSummaryRule {
    override fun check(item: SeasonalDeclarationSummary): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class SeasonalDeclarationSummaryCodeLengthRule(var maxLength: Int) : SeasonalDeclarationSummaryRule {
    override fun check(item: SeasonalDeclarationSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDeclarationSummaryDiscountRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalDeclarationSummaryRule {
    override fun check(item: SeasonalDeclarationSummary): MutableList<String> {
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

/** Runs every rule of a seasonal declaration summary. */
data class SeasonalDeclarationSummaryValidator(var rules: MutableList<SeasonalDeclarationSummaryRule>) {
    fun validate(item: SeasonalDeclarationSummary): MutableList<String> {
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

    fun requireValid(item: SeasonalDeclarationSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDeclarationSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDeclarationSummaryValidator(): SeasonalDeclarationSummaryValidator {
    return SeasonalDeclarationSummaryValidator(
        mutableListOf<SeasonalDeclarationSummaryRule>(
            SeasonalDeclarationSummaryRequiredTitleRule("seasonal declaration summary needs this field"),
            SeasonalDeclarationSummaryAllowedCodeRule(mutableListOf<String>("rolling", "seasonal", "strict")),
            SeasonalDeclarationSummaryQuantityRangeRule(16, 64),
            SeasonalDeclarationSummaryCodeLengthRule(1000),
            SeasonalDeclarationSummaryDiscountRangeRule(3.5, 325.9),
        ),
    )
}
