package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.kit.text.normalize

/** A rule a batched audit quota must pass. Check returns its problems, or none. */
interface BatchedAuditQuotaRule {
    fun check(item: BatchedAuditQuota): MutableList<String>
}

data class BatchedAuditQuotaAllowedRegionRule(var allowed: MutableList<String>) : BatchedAuditQuotaRule {
    override fun check(item: BatchedAuditQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAuditQuotaDurationMinutesRangeRule(var min: Int, var max: Int) : BatchedAuditQuotaRule {
    override fun check(item: BatchedAuditQuota): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAuditQuotaCodeLengthRule(var maxLength: Int) : BatchedAuditQuotaRule {
    override fun check(item: BatchedAuditQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAuditQuotaRequiredCodeRule(var message: String) : BatchedAuditQuotaRule {
    override fun check(item: BatchedAuditQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAuditQuotaRequiredContactEmailRule(var message: String) : BatchedAuditQuotaRule {
    override fun check(item: BatchedAuditQuota): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched audit quota. */
data class BatchedAuditQuotaValidator(var rules: MutableList<BatchedAuditQuotaRule>) {
    fun validate(item: BatchedAuditQuota): MutableList<String> {
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

    fun requireValid(item: BatchedAuditQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAuditQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAuditQuotaValidator(): BatchedAuditQuotaValidator {
    return BatchedAuditQuotaValidator(
        mutableListOf<BatchedAuditQuotaRule>(
            BatchedAuditQuotaAllowedRegionRule(mutableListOf<String>()),
            BatchedAuditQuotaDurationMinutesRangeRule(64, 1000),
            BatchedAuditQuotaCodeLengthRule(120),
            BatchedAuditQuotaRequiredCodeRule("batched audit quota needs a value"),
            BatchedAuditQuotaRequiredContactEmailRule("batched audit quota needs this field"),
        ),
    )
}
