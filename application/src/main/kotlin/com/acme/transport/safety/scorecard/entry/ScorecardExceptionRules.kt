package com.acme.transport.safety.scorecard.entry

import com.acme.transport.kit.text.normalize

/** A rule a scorecard exception must pass. Check returns its problems, or none. */
interface ScorecardExceptionRule {
    fun check(item: ScorecardException): MutableList<String>
}

data class ScorecardExceptionRequiredLocaleRule(var message: String) : ScorecardExceptionRule {
    override fun check(item: ScorecardException): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardExceptionLocaleLengthRule(var maxLength: Int) : ScorecardExceptionRule {
    override fun check(item: ScorecardException): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardExceptionAllowedDockRule(var allowed: MutableList<String>) : ScorecardExceptionRule {
    override fun check(item: ScorecardException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard exception. */
data class ScorecardExceptionValidator(var rules: MutableList<ScorecardExceptionRule>) {
    fun validate(item: ScorecardException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScorecardException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardException>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardExceptionValidator(): ScorecardExceptionValidator {
    return ScorecardExceptionValidator(
        mutableListOf<ScorecardExceptionRule>(
            ScorecardExceptionRequiredLocaleRule("scorecard exception needs a value"),
            ScorecardExceptionLocaleLengthRule(86400),
            ScorecardExceptionAllowedDockRule(mutableListOf<String>("default", "primary", "global")),
        ),
    )
}
