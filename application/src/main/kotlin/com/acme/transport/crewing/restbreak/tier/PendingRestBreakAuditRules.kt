package com.acme.transport.crewing.restbreak.tier

import com.acme.transport.kit.text.normalize

/** A rule a pending rest break audit must pass. Check returns its problems, or none. */
interface PendingRestBreakAuditRule {
    fun check(item: PendingRestBreakAudit): MutableList<String>
}

data class PendingRestBreakAuditRequiredSummaryRule(var message: String) : PendingRestBreakAuditRule {
    override fun check(item: PendingRestBreakAudit): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRestBreakAuditCodeLengthRule(var maxLength: Int) : PendingRestBreakAuditRule {
    override fun check(item: PendingRestBreakAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRestBreakAuditDescriptionLengthRule(var maxLength: Int) : PendingRestBreakAuditRule {
    override fun check(item: PendingRestBreakAudit): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRestBreakAuditCodePrefixRule(var prefix: String) : PendingRestBreakAuditRule {
    override fun check(item: PendingRestBreakAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingRestBreakAuditBatchSizeRangeRule(var min: Int, var max: Int) : PendingRestBreakAuditRule {
    override fun check(item: PendingRestBreakAudit): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class PendingRestBreakAuditAllowedCodeRule(var allowed: MutableList<String>) : PendingRestBreakAuditRule {
    override fun check(item: PendingRestBreakAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending rest break audit. */
data class PendingRestBreakAuditValidator(var rules: MutableList<PendingRestBreakAuditRule>) {
    fun validate(item: PendingRestBreakAudit): MutableList<String> {
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

    fun requireValid(item: PendingRestBreakAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingRestBreakAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingRestBreakAuditValidator(): PendingRestBreakAuditValidator {
    return PendingRestBreakAuditValidator(
        mutableListOf<PendingRestBreakAuditRule>(
            PendingRestBreakAuditRequiredSummaryRule("pending rest break audit needs a value"),
            PendingRestBreakAuditCodeLengthRule(16),
            PendingRestBreakAuditDescriptionLengthRule(16),
            PendingRestBreakAuditCodePrefixRule("Q"),
            PendingRestBreakAuditBatchSizeRangeRule(64, 86400),
            PendingRestBreakAuditAllowedCodeRule(mutableListOf<String>("global", "legacy", "capped")),
        ),
    )
}
