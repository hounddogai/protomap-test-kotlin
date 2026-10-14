package com.acme.transport.safety.coaching.template

import com.acme.transport.kit.text.normalize

/** A rule a primary coaching ledger must pass. Check returns its problems, or none. */
interface PrimaryCoachingLedgerRule {
    fun check(item: PrimaryCoachingLedger): MutableList<String>
}

data class PrimaryCoachingLedgerCodePrefixRule(var prefix: String) : PrimaryCoachingLedgerRule {
    override fun check(item: PrimaryCoachingLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCoachingLedgerAllowedTrailerIdRule(
    var allowed: MutableList<String>,
) : PrimaryCoachingLedgerRule {
    override fun check(item: PrimaryCoachingLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCoachingLedgerTitleLengthRule(var maxLength: Int) : PrimaryCoachingLedgerRule {
    override fun check(item: PrimaryCoachingLedger): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCoachingLedgerTrailerIdLengthRule(var maxLength: Int) : PrimaryCoachingLedgerRule {
    override fun check(item: PrimaryCoachingLedger): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary coaching ledger. */
data class PrimaryCoachingLedgerValidator(var rules: MutableList<PrimaryCoachingLedgerRule>) {
    fun validate(item: PrimaryCoachingLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryCoachingLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCoachingLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCoachingLedgerValidator(): PrimaryCoachingLedgerValidator {
    return PrimaryCoachingLedgerValidator(
        mutableListOf<PrimaryCoachingLedgerRule>(
            PrimaryCoachingLedgerCodePrefixRule("REF"),
            PrimaryCoachingLedgerAllowedTrailerIdRule(mutableListOf<String>("adaptive", "capped", "seasonal")),
            PrimaryCoachingLedgerTitleLengthRule(64),
            PrimaryCoachingLedgerTrailerIdLengthRule(64),
        ),
    )
}
