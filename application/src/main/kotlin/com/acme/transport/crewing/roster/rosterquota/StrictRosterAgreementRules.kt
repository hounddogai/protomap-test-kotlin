package com.acme.transport.crewing.roster.rosterquota

/** A rule a strict roster agreement must pass. Check returns its problems, or none. */
interface StrictRosterAgreementRule {
    fun check(item: StrictRosterAgreement): MutableList<String>
}

data class StrictRosterAgreementRequiredCurrencyRule(var message: String) : StrictRosterAgreementRule {
    override fun check(item: StrictRosterAgreement): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictRosterAgreementStartsAtRangeRule(var min: Int, var max: Int) : StrictRosterAgreementRule {
    override fun check(item: StrictRosterAgreement): MutableList<String> {
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

data class StrictRosterAgreementRequiredCodeRule(var message: String) : StrictRosterAgreementRule {
    override fun check(item: StrictRosterAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict roster agreement. */
data class StrictRosterAgreementValidator(var rules: MutableList<StrictRosterAgreementRule>) {
    fun validate(item: StrictRosterAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictRosterAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRosterAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRosterAgreementValidator(): StrictRosterAgreementValidator {
    return StrictRosterAgreementValidator(
        mutableListOf<StrictRosterAgreementRule>(
            StrictRosterAgreementRequiredCurrencyRule("strict roster agreement needs an entry"),
            StrictRosterAgreementStartsAtRangeRule(80, 120),
            StrictRosterAgreementRequiredCodeRule("strict roster agreement needs a value"),
        ),
    )
}
