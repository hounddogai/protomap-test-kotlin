package com.acme.transport.crewing.roster.rosteragreement

/** A rule a default roster agreement must pass. Check returns its problems, or none. */
interface DefaultRosterAgreementRule {
    fun check(item: DefaultRosterAgreement): MutableList<String>
}

data class DefaultRosterAgreementEndsAtRangeRule(var min: Int, var max: Int) : DefaultRosterAgreementRule {
    override fun check(item: DefaultRosterAgreement): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class DefaultRosterAgreementContactEmailLengthRule(var maxLength: Int) : DefaultRosterAgreementRule {
    override fun check(item: DefaultRosterAgreement): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRosterAgreementRequiredCodeRule(var message: String) : DefaultRosterAgreementRule {
    override fun check(item: DefaultRosterAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default roster agreement. */
data class DefaultRosterAgreementValidator(var rules: MutableList<DefaultRosterAgreementRule>) {
    fun validate(item: DefaultRosterAgreement): MutableList<String> {
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

    fun requireValid(item: DefaultRosterAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRosterAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRosterAgreementValidator(): DefaultRosterAgreementValidator {
    return DefaultRosterAgreementValidator(
        mutableListOf<DefaultRosterAgreementRule>(
            DefaultRosterAgreementEndsAtRangeRule(64, 255),
            DefaultRosterAgreementContactEmailLengthRule(3600),
            DefaultRosterAgreementRequiredCodeRule("default roster agreement needs a value"),
        ),
    )
}
