package com.acme.transport.crewing.qualification.rule

/** A rule a rolling qualification rule must pass. Check returns its problems, or none. */
interface RollingQualificationRuleRule {
    fun check(item: RollingQualificationRule): MutableList<String>
}

data class RollingQualificationRuleRequiredDepotRule(var message: String) : RollingQualificationRuleRule {
    override fun check(item: RollingQualificationRule): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingQualificationRuleStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : RollingQualificationRuleRule {
    override fun check(item: RollingQualificationRule): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class RollingQualificationRuleCountryLengthRule(var maxLength: Int) : RollingQualificationRuleRule {
    override fun check(item: RollingQualificationRule): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling qualification rule. */
data class RollingQualificationRuleValidator(var rules: MutableList<RollingQualificationRuleRule>) {
    fun validate(item: RollingQualificationRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingQualificationRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingQualificationRule>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingQualificationRuleValidator(): RollingQualificationRuleValidator {
    return RollingQualificationRuleValidator(
        mutableListOf<RollingQualificationRuleRule>(
            RollingQualificationRuleRequiredDepotRule("rolling qualification rule needs a value"),
            RollingQualificationRuleStartsAtRangeRule(80, 86400),
            RollingQualificationRuleCountryLengthRule(32),
        ),
    )
}
