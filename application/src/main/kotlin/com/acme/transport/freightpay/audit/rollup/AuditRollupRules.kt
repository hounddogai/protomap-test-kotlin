package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.kit.text.normalize

/** A rule a audit rollup must pass. Check returns its problems, or none. */
interface AuditRollupRule {
    fun check(item: AuditRollup): MutableList<String>
}

data class AuditRollupExternalRefLengthRule(var maxLength: Int) : AuditRollupRule {
    override fun check(item: AuditRollup): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditRollupRetriesRangeRule(var min: Int, var max: Int) : AuditRollupRule {
    override fun check(item: AuditRollup): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class AuditRollupAllowedCodeRule(var allowed: MutableList<String>) : AuditRollupRule {
    override fun check(item: AuditRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class AuditRollupBatchSizeRangeRule(var min: Int, var max: Int) : AuditRollupRule {
    override fun check(item: AuditRollup): MutableList<String> {
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

data class AuditRollupSummaryLengthRule(var maxLength: Int) : AuditRollupRule {
    override fun check(item: AuditRollup): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditRollupTitleLengthRule(var maxLength: Int) : AuditRollupRule {
    override fun check(item: AuditRollup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit rollup. */
data class AuditRollupValidator(var rules: MutableList<AuditRollupRule>) {
    fun validate(item: AuditRollup): MutableList<String> {
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

    fun requireValid(item: AuditRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditRollupValidator(): AuditRollupValidator {
    return AuditRollupValidator(
        mutableListOf<AuditRollupRule>(
            AuditRollupExternalRefLengthRule(255),
            AuditRollupRetriesRangeRule(64, 120),
            AuditRollupAllowedCodeRule(mutableListOf<String>("seasonal", "lenient", "rolling")),
            AuditRollupBatchSizeRangeRule(64, 1000),
            AuditRollupSummaryLengthRule(255),
            AuditRollupTitleLengthRule(80),
        ),
    )
}
