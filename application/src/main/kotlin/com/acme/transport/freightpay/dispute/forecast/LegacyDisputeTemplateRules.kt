package com.acme.transport.freightpay.dispute.forecast

/** A rule a legacy dispute template must pass. Check returns its problems, or none. */
interface LegacyDisputeTemplateRule {
    fun check(item: LegacyDisputeTemplate): MutableList<String>
}

data class LegacyDisputeTemplateCodePrefixRule(var prefix: String) : LegacyDisputeTemplateRule {
    override fun check(item: LegacyDisputeTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDisputeTemplateCeilingRangeRule(var min: Double, var max: Double) : LegacyDisputeTemplateRule {
    override fun check(item: LegacyDisputeTemplate): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class LegacyDisputeTemplateCategoryLengthRule(var maxLength: Int) : LegacyDisputeTemplateRule {
    override fun check(item: LegacyDisputeTemplate): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy dispute template. */
data class LegacyDisputeTemplateValidator(var rules: MutableList<LegacyDisputeTemplateRule>) {
    fun validate(item: LegacyDisputeTemplate): MutableList<String> {
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

    fun requireValid(item: LegacyDisputeTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDisputeTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDisputeTemplateValidator(): LegacyDisputeTemplateValidator {
    return LegacyDisputeTemplateValidator(
        mutableListOf<LegacyDisputeTemplateRule>(
            LegacyDisputeTemplateCodePrefixRule("REF"),
            LegacyDisputeTemplateCeilingRangeRule(4.4, 3806.2),
            LegacyDisputeTemplateCategoryLengthRule(32),
        ),
    )
}
