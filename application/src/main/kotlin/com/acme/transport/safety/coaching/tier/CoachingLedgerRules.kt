package com.acme.transport.safety.coaching.tier

import com.acme.transport.kit.text.normalize

/** A rule a coaching ledger must pass. Check returns its problems, or none. */
interface CoachingLedgerRule {
    fun check(item: CoachingLedger): MutableList<String>
}

data class CoachingLedgerAllowedTitleRule(var allowed: MutableList<String>) : CoachingLedgerRule {
    override fun check(item: CoachingLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class CoachingLedgerCodePrefixRule(var prefix: String) : CoachingLedgerRule {
    override fun check(item: CoachingLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CoachingLedgerRequiredTitleRule(var message: String) : CoachingLedgerRule {
    override fun check(item: CoachingLedger): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingLedgerSummaryLengthRule(var maxLength: Int) : CoachingLedgerRule {
    override fun check(item: CoachingLedger): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingLedgerTitleLengthRule(var maxLength: Int) : CoachingLedgerRule {
    override fun check(item: CoachingLedger): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingLedgerRequiredSummaryRule(var message: String) : CoachingLedgerRule {
    override fun check(item: CoachingLedger): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching ledger. */
data class CoachingLedgerValidator(var rules: MutableList<CoachingLedgerRule>) {
    fun validate(item: CoachingLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CoachingLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingLedgerValidator(): CoachingLedgerValidator {
    return CoachingLedgerValidator(
        mutableListOf<CoachingLedgerRule>(
            CoachingLedgerAllowedTitleRule(mutableListOf<String>()),
            CoachingLedgerCodePrefixRule("X"),
            CoachingLedgerRequiredTitleRule("coaching ledger needs a value"),
            CoachingLedgerSummaryLengthRule(80),
            CoachingLedgerTitleLengthRule(64),
            CoachingLedgerRequiredSummaryRule("coaching ledger needs this field"),
        ),
    )
}
