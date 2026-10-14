package com.acme.transport.safety.coaching.digest

/** A rule a coaching agreement must pass. Check returns its problems, or none. */
interface CoachingAgreementRule {
    fun check(item: CoachingAgreement): MutableList<String>
}

data class CoachingAgreementThresholdRangeRule(var min: Double, var max: Double) : CoachingAgreementRule {
    override fun check(item: CoachingAgreement): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class CoachingAgreementRequiredNameRule(var message: String) : CoachingAgreementRule {
    override fun check(item: CoachingAgreement): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingAgreementCodePrefixRule(var prefix: String) : CoachingAgreementRule {
    override fun check(item: CoachingAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CoachingAgreementRequiredCodeRule(var message: String) : CoachingAgreementRule {
    override fun check(item: CoachingAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching agreement. */
data class CoachingAgreementValidator(var rules: MutableList<CoachingAgreementRule>) {
    fun validate(item: CoachingAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CoachingAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingAgreementValidator(): CoachingAgreementValidator {
    return CoachingAgreementValidator(
        mutableListOf<CoachingAgreementRule>(
            CoachingAgreementThresholdRangeRule(8.8, 1197.3),
            CoachingAgreementRequiredNameRule("coaching agreement needs a value"),
            CoachingAgreementCodePrefixRule("X"),
            CoachingAgreementRequiredCodeRule("coaching agreement needs an entry"),
        ),
    )
}
