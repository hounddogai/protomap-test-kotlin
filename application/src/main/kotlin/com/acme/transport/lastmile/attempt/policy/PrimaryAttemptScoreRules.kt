package com.acme.transport.lastmile.attempt.policy

/** A rule a primary attempt score must pass. Check returns its problems, or none. */
interface PrimaryAttemptScoreRule {
    fun check(item: PrimaryAttemptScore): MutableList<String>
}

data class PrimaryAttemptScoreCountryLengthRule(var maxLength: Int) : PrimaryAttemptScoreRule {
    override fun check(item: PrimaryAttemptScore): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAttemptScoreRequiredCodeRule(var message: String) : PrimaryAttemptScoreRule {
    override fun check(item: PrimaryAttemptScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAttemptScoreRequiredCountryRule(var message: String) : PrimaryAttemptScoreRule {
    override fun check(item: PrimaryAttemptScore): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary attempt score. */
data class PrimaryAttemptScoreValidator(var rules: MutableList<PrimaryAttemptScoreRule>) {
    fun validate(item: PrimaryAttemptScore): MutableList<String> {
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

    fun requireValid(item: PrimaryAttemptScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryAttemptScore>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryAttemptScoreValidator(): PrimaryAttemptScoreValidator {
    return PrimaryAttemptScoreValidator(
        mutableListOf<PrimaryAttemptScoreRule>(
            PrimaryAttemptScoreCountryLengthRule(86400),
            PrimaryAttemptScoreRequiredCodeRule("primary attempt score needs a value"),
            PrimaryAttemptScoreRequiredCountryRule("primary attempt score needs this field"),
        ),
    )
}
