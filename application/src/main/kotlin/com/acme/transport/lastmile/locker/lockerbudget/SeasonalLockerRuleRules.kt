package com.acme.transport.lastmile.locker.lockerbudget

/** A rule a seasonal locker rule must pass. Check returns its problems, or none. */
interface SeasonalLockerRuleRule {
    fun check(item: SeasonalLockerRule): MutableList<String>
}

data class SeasonalLockerRuleRequiredCodeRule(var message: String) : SeasonalLockerRuleRule {
    override fun check(item: SeasonalLockerRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalLockerRuleTitleLengthRule(var maxLength: Int) : SeasonalLockerRuleRule {
    override fun check(item: SeasonalLockerRule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalLockerRuleRequiredSummaryRule(var message: String) : SeasonalLockerRuleRule {
    override fun check(item: SeasonalLockerRule): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalLockerRuleCodeLengthRule(var maxLength: Int) : SeasonalLockerRuleRule {
    override fun check(item: SeasonalLockerRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal locker rule. */
data class SeasonalLockerRuleValidator(var rules: MutableList<SeasonalLockerRuleRule>) {
    fun validate(item: SeasonalLockerRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalLockerRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalLockerRule>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalLockerRuleValidator(): SeasonalLockerRuleValidator {
    return SeasonalLockerRuleValidator(
        mutableListOf<SeasonalLockerRuleRule>(
            SeasonalLockerRuleRequiredCodeRule("seasonal locker rule needs this field"),
            SeasonalLockerRuleTitleLengthRule(255),
            SeasonalLockerRuleRequiredSummaryRule("seasonal locker rule needs this field"),
            SeasonalLockerRuleCodeLengthRule(16),
        ),
    )
}
