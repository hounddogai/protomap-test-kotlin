package com.acme.transport.safety.coaching.entry

/** A rule a strict coaching agreement must pass. Check returns its problems, or none. */
interface StrictCoachingAgreementRule {
    fun check(item: StrictCoachingAgreement): MutableList<String>
}

data class StrictCoachingAgreementMarginRangeRule(
    var min: Double,
    var max: Double,
) : StrictCoachingAgreementRule {
    override fun check(item: StrictCoachingAgreement): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictCoachingAgreementAttemptsRangeRule(var min: Int, var max: Int) : StrictCoachingAgreementRule {
    override fun check(item: StrictCoachingAgreement): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class StrictCoachingAgreementCodePrefixRule(var prefix: String) : StrictCoachingAgreementRule {
    override fun check(item: StrictCoachingAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictCoachingAgreementStartsAtRangeRule(var min: Int, var max: Int) : StrictCoachingAgreementRule {
    override fun check(item: StrictCoachingAgreement): MutableList<String> {
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

data class StrictCoachingAgreementDepotLengthRule(var maxLength: Int) : StrictCoachingAgreementRule {
    override fun check(item: StrictCoachingAgreement): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict coaching agreement. */
data class StrictCoachingAgreementValidator(var rules: MutableList<StrictCoachingAgreementRule>) {
    fun validate(item: StrictCoachingAgreement): MutableList<String> {
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

    fun requireValid(item: StrictCoachingAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictCoachingAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictCoachingAgreementValidator(): StrictCoachingAgreementValidator {
    return StrictCoachingAgreementValidator(
        mutableListOf<StrictCoachingAgreementRule>(
            StrictCoachingAgreementMarginRangeRule(7.3, 1685.0),
            StrictCoachingAgreementAttemptsRangeRule(16, 86400),
            StrictCoachingAgreementCodePrefixRule("ACM"),
            StrictCoachingAgreementStartsAtRangeRule(86400, 86400),
            StrictCoachingAgreementDepotLengthRule(255),
        ),
    )
}
