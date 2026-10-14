package com.acme.transport.customs.declaration.summary

/** A rule a declaration summary must pass. Check returns its problems, or none. */
interface DeclarationSummaryRule {
    fun check(item: DeclarationSummary): MutableList<String>
}

data class DeclarationSummaryCodeLengthRule(var maxLength: Int) : DeclarationSummaryRule {
    override fun check(item: DeclarationSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationSummaryWindowDaysRangeRule(var min: Int, var max: Int) : DeclarationSummaryRule {
    override fun check(item: DeclarationSummary): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationSummaryCodePrefixRule(var prefix: String) : DeclarationSummaryRule {
    override fun check(item: DeclarationSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration summary. */
data class DeclarationSummaryValidator(var rules: MutableList<DeclarationSummaryRule>) {
    fun validate(item: DeclarationSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DeclarationSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationSummaryValidator(): DeclarationSummaryValidator {
    return DeclarationSummaryValidator(
        mutableListOf<DeclarationSummaryRule>(
            DeclarationSummaryCodeLengthRule(120),
            DeclarationSummaryWindowDaysRangeRule(16, 64),
            DeclarationSummaryCodePrefixRule("Q"),
        ),
    )
}
