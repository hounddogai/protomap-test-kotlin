package com.acme.transport.crewing.roster.rosteragreement

/** A rule a legacy roster template must pass. Check returns its problems, or none. */
interface LegacyRosterTemplateRule {
    fun check(item: LegacyRosterTemplate): MutableList<String>
}

data class LegacyRosterTemplateGraceDaysRangeRule(var min: Int, var max: Int) : LegacyRosterTemplateRule {
    override fun check(item: LegacyRosterTemplate): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class LegacyRosterTemplateDiscountRangeRule(var min: Double, var max: Double) : LegacyRosterTemplateRule {
    override fun check(item: LegacyRosterTemplate): MutableList<String> {
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

data class LegacyRosterTemplateRouteCodeLengthRule(var maxLength: Int) : LegacyRosterTemplateRule {
    override fun check(item: LegacyRosterTemplate): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRosterTemplateRequiredTitleRule(var message: String) : LegacyRosterTemplateRule {
    override fun check(item: LegacyRosterTemplate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy roster template. */
data class LegacyRosterTemplateValidator(var rules: MutableList<LegacyRosterTemplateRule>) {
    fun validate(item: LegacyRosterTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyRosterTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyRosterTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyRosterTemplateValidator(): LegacyRosterTemplateValidator {
    return LegacyRosterTemplateValidator(
        mutableListOf<LegacyRosterTemplateRule>(
            LegacyRosterTemplateGraceDaysRangeRule(80, 3600),
            LegacyRosterTemplateDiscountRangeRule(6.1, 1352.2),
            LegacyRosterTemplateRouteCodeLengthRule(86400),
            LegacyRosterTemplateRequiredTitleRule("legacy roster template needs an entry"),
        ),
    )
}
