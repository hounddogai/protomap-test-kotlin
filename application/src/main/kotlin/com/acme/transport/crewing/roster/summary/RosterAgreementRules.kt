package com.acme.transport.crewing.roster.summary

import com.acme.transport.kit.text.normalize

/** A rule a roster agreement must pass. Check returns its problems, or none. */
interface RosterAgreementRule {
    fun check(item: RosterAgreement): MutableList<String>
}

data class RosterAgreementAllowedTitleRule(var allowed: MutableList<String>) : RosterAgreementRule {
    override fun check(item: RosterAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class RosterAgreementMinimumRangeRule(var min: Double, var max: Double) : RosterAgreementRule {
    override fun check(item: RosterAgreement): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class RosterAgreementRequiredLocaleRule(var message: String) : RosterAgreementRule {
    override fun check(item: RosterAgreement): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster agreement. */
data class RosterAgreementValidator(var rules: MutableList<RosterAgreementRule>) {
    fun validate(item: RosterAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterAgreementValidator(): RosterAgreementValidator {
    return RosterAgreementValidator(
        mutableListOf<RosterAgreementRule>(
            RosterAgreementAllowedTitleRule(mutableListOf<String>("capped", "batched", "seasonal")),
            RosterAgreementMinimumRangeRule(8.1, 4997.7),
            RosterAgreementRequiredLocaleRule("roster agreement needs a value"),
        ),
    )
}
