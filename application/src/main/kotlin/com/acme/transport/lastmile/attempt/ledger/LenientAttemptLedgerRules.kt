package com.acme.transport.lastmile.attempt.ledger

import com.acme.transport.kit.text.normalize

/** A rule a lenient attempt ledger must pass. Check returns its problems, or none. */
interface LenientAttemptLedgerRule {
    fun check(item: LenientAttemptLedger): MutableList<String>
}

data class LenientAttemptLedgerRequiredCodeRule(var message: String) : LenientAttemptLedgerRule {
    override fun check(item: LenientAttemptLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientAttemptLedgerRequiredChannelRule(var message: String) : LenientAttemptLedgerRule {
    override fun check(item: LenientAttemptLedger): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientAttemptLedgerAllowedTitleRule(var allowed: MutableList<String>) : LenientAttemptLedgerRule {
    override fun check(item: LenientAttemptLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class LenientAttemptLedgerVersionRangeRule(var min: Int, var max: Int) : LenientAttemptLedgerRule {
    override fun check(item: LenientAttemptLedger): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientAttemptLedgerCodeLengthRule(var maxLength: Int) : LenientAttemptLedgerRule {
    override fun check(item: LenientAttemptLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientAttemptLedgerEtaMinutesRangeRule(var min: Int, var max: Int) : LenientAttemptLedgerRule {
    override fun check(item: LenientAttemptLedger): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient attempt ledger. */
data class LenientAttemptLedgerValidator(var rules: MutableList<LenientAttemptLedgerRule>) {
    fun validate(item: LenientAttemptLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientAttemptLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientAttemptLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientAttemptLedgerValidator(): LenientAttemptLedgerValidator {
    return LenientAttemptLedgerValidator(
        mutableListOf<LenientAttemptLedgerRule>(
            LenientAttemptLedgerRequiredCodeRule("lenient attempt ledger needs a value"),
            LenientAttemptLedgerRequiredChannelRule("lenient attempt ledger needs this field"),
            LenientAttemptLedgerAllowedTitleRule(mutableListOf<String>("global", "default", "fallback")),
            LenientAttemptLedgerVersionRangeRule(32, 1000),
            LenientAttemptLedgerCodeLengthRule(80),
            LenientAttemptLedgerEtaMinutesRangeRule(64, 80),
        ),
    )
}
