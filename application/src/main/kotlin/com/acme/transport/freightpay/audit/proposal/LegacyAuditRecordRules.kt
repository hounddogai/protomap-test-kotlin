package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.kit.text.normalize

/** A rule a legacy audit record must pass. Check returns its problems, or none. */
interface LegacyAuditRecordRule {
    fun check(item: LegacyAuditRecord): MutableList<String>
}

data class LegacyAuditRecordRequiredLabelRule(var message: String) : LegacyAuditRecordRule {
    override fun check(item: LegacyAuditRecord): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyAuditRecordLabelLengthRule(var maxLength: Int) : LegacyAuditRecordRule {
    override fun check(item: LegacyAuditRecord): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAuditRecordSegmentLengthRule(var maxLength: Int) : LegacyAuditRecordRule {
    override fun check(item: LegacyAuditRecord): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAuditRecordAllowedSegmentRule(var allowed: MutableList<String>) : LegacyAuditRecordRule {
    override fun check(item: LegacyAuditRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is expired")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAuditRecordBatchSizeRangeRule(var min: Int, var max: Int) : LegacyAuditRecordRule {
    override fun check(item: LegacyAuditRecord): MutableList<String> {
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

data class LegacyAuditRecordCodeLengthRule(var maxLength: Int) : LegacyAuditRecordRule {
    override fun check(item: LegacyAuditRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy audit record. */
data class LegacyAuditRecordValidator(var rules: MutableList<LegacyAuditRecordRule>) {
    fun validate(item: LegacyAuditRecord): MutableList<String> {
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

    fun requireValid(item: LegacyAuditRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyAuditRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyAuditRecordValidator(): LegacyAuditRecordValidator {
    return LegacyAuditRecordValidator(
        mutableListOf<LegacyAuditRecordRule>(
            LegacyAuditRecordRequiredLabelRule("legacy audit record needs this field"),
            LegacyAuditRecordLabelLengthRule(86400),
            LegacyAuditRecordSegmentLengthRule(86400),
            LegacyAuditRecordAllowedSegmentRule(mutableListOf<String>("regional", "scheduled")),
            LegacyAuditRecordBatchSizeRangeRule(64, 255),
            LegacyAuditRecordCodeLengthRule(120),
        ),
    )
}
