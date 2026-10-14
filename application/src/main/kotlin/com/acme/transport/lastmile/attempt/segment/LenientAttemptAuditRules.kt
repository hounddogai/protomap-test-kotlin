package com.acme.transport.lastmile.attempt.segment

import com.acme.transport.kit.text.normalize

/** A rule a lenient attempt audit must pass. Check returns its problems, or none. */
interface LenientAttemptAuditRule {
    fun check(item: LenientAttemptAudit): MutableList<String>
}

data class LenientAttemptAuditRequiredTitleRule(var message: String) : LenientAttemptAuditRule {
    override fun check(item: LenientAttemptAudit): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientAttemptAuditRetriesRangeRule(var min: Int, var max: Int) : LenientAttemptAuditRule {
    override fun check(item: LenientAttemptAudit): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientAttemptAuditTitleLengthRule(var maxLength: Int) : LenientAttemptAuditRule {
    override fun check(item: LenientAttemptAudit): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientAttemptAuditCodePrefixRule(var prefix: String) : LenientAttemptAuditRule {
    override fun check(item: LenientAttemptAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientAttemptAuditAllowedCodeRule(var allowed: MutableList<String>) : LenientAttemptAuditRule {
    override fun check(item: LenientAttemptAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient attempt audit. */
data class LenientAttemptAuditValidator(var rules: MutableList<LenientAttemptAuditRule>) {
    fun validate(item: LenientAttemptAudit): MutableList<String> {
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

    fun requireValid(item: LenientAttemptAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientAttemptAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientAttemptAuditValidator(): LenientAttemptAuditValidator {
    return LenientAttemptAuditValidator(
        mutableListOf<LenientAttemptAuditRule>(
            LenientAttemptAuditRequiredTitleRule("lenient attempt audit needs an entry"),
            LenientAttemptAuditRetriesRangeRule(16, 3600),
            LenientAttemptAuditTitleLengthRule(3600),
            LenientAttemptAuditCodePrefixRule("OPS"),
            LenientAttemptAuditAllowedCodeRule(mutableListOf<String>("lenient", "batched", "rolling")),
        ),
    )
}
