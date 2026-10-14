package com.acme.transport.lastmile.attempt.window

/** A rule a default attempt claim must pass. Check returns its problems, or none. */
interface DefaultAttemptClaimRule {
    fun check(item: DefaultAttemptClaim): MutableList<String>
}

data class DefaultAttemptClaimQuantityRangeRule(var min: Int, var max: Int) : DefaultAttemptClaimRule {
    override fun check(item: DefaultAttemptClaim): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAttemptClaimGraceDaysRangeRule(var min: Int, var max: Int) : DefaultAttemptClaimRule {
    override fun check(item: DefaultAttemptClaim): MutableList<String> {
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

data class DefaultAttemptClaimCountryLengthRule(var maxLength: Int) : DefaultAttemptClaimRule {
    override fun check(item: DefaultAttemptClaim): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAttemptClaimTitleLengthRule(var maxLength: Int) : DefaultAttemptClaimRule {
    override fun check(item: DefaultAttemptClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAttemptClaimSegmentLengthRule(var maxLength: Int) : DefaultAttemptClaimRule {
    override fun check(item: DefaultAttemptClaim): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default attempt claim. */
data class DefaultAttemptClaimValidator(var rules: MutableList<DefaultAttemptClaimRule>) {
    fun validate(item: DefaultAttemptClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultAttemptClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultAttemptClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultAttemptClaimValidator(): DefaultAttemptClaimValidator {
    return DefaultAttemptClaimValidator(
        mutableListOf<DefaultAttemptClaimRule>(
            DefaultAttemptClaimQuantityRangeRule(1000, 3600),
            DefaultAttemptClaimGraceDaysRangeRule(64, 64),
            DefaultAttemptClaimCountryLengthRule(80),
            DefaultAttemptClaimTitleLengthRule(86400),
            DefaultAttemptClaimSegmentLengthRule(3600),
        ),
    )
}
