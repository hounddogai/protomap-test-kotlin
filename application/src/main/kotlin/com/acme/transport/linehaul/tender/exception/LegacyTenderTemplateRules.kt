package com.acme.transport.linehaul.tender.exception

/** A rule a legacy tender template must pass. Check returns its problems, or none. */
interface LegacyTenderTemplateRule {
    fun check(item: LegacyTenderTemplate): MutableList<String>
}

data class LegacyTenderTemplateBudgetRangeRule(var min: Double, var max: Double) : LegacyTenderTemplateRule {
    override fun check(item: LegacyTenderTemplate): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTenderTemplateDriverIdLengthRule(var maxLength: Int) : LegacyTenderTemplateRule {
    override fun check(item: LegacyTenderTemplate): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTenderTemplateCodePrefixRule(var prefix: String) : LegacyTenderTemplateRule {
    override fun check(item: LegacyTenderTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTenderTemplateRequiredDriverIdRule(var message: String) : LegacyTenderTemplateRule {
    override fun check(item: LegacyTenderTemplate): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy tender template. */
data class LegacyTenderTemplateValidator(var rules: MutableList<LegacyTenderTemplateRule>) {
    fun validate(item: LegacyTenderTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyTenderTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTenderTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTenderTemplateValidator(): LegacyTenderTemplateValidator {
    return LegacyTenderTemplateValidator(
        mutableListOf<LegacyTenderTemplateRule>(
            LegacyTenderTemplateBudgetRangeRule(9.5, 2914.9),
            LegacyTenderTemplateDriverIdLengthRule(16),
            LegacyTenderTemplateCodePrefixRule("OPS"),
            LegacyTenderTemplateRequiredDriverIdRule("legacy tender template needs an entry"),
        ),
    )
}
