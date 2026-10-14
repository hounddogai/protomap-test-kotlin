package com.acme.transport.safety.coaching.agreement

/** A rule a regional coaching agreement must pass. Check returns its problems, or none. */
interface RegionalCoachingAgreementRule {
    fun check(item: RegionalCoachingAgreement): MutableList<String>
}

data class RegionalCoachingAgreementUnitLengthRule(var maxLength: Int) : RegionalCoachingAgreementRule {
    override fun check(item: RegionalCoachingAgreement): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCoachingAgreementRequiredCodeRule(var message: String) : RegionalCoachingAgreementRule {
    override fun check(item: RegionalCoachingAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCoachingAgreementRequiredTitleRule(var message: String) : RegionalCoachingAgreementRule {
    override fun check(item: RegionalCoachingAgreement): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCoachingAgreementCodeLengthRule(var maxLength: Int) : RegionalCoachingAgreementRule {
    override fun check(item: RegionalCoachingAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional coaching agreement. */
data class RegionalCoachingAgreementValidator(var rules: MutableList<RegionalCoachingAgreementRule>) {
    fun validate(item: RegionalCoachingAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalCoachingAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCoachingAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCoachingAgreementValidator(): RegionalCoachingAgreementValidator {
    return RegionalCoachingAgreementValidator(
        mutableListOf<RegionalCoachingAgreementRule>(
            RegionalCoachingAgreementUnitLengthRule(32),
            RegionalCoachingAgreementRequiredCodeRule("regional coaching agreement needs this field"),
            RegionalCoachingAgreementRequiredTitleRule("regional coaching agreement needs an entry"),
            RegionalCoachingAgreementCodeLengthRule(1000),
        ),
    )
}
