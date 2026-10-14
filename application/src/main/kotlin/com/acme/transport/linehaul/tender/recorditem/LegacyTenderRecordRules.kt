package com.acme.transport.linehaul.tender.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a legacy tender record must pass. Check returns its problems, or none. */
interface LegacyTenderRecordRule {
    fun check(item: LegacyTenderRecord): MutableList<String>
}

data class LegacyTenderRecordChecksumLengthRule(var maxLength: Int) : LegacyTenderRecordRule {
    override fun check(item: LegacyTenderRecord): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTenderRecordTitleLengthRule(var maxLength: Int) : LegacyTenderRecordRule {
    override fun check(item: LegacyTenderRecord): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTenderRecordRequiredChecksumRule(var message: String) : LegacyTenderRecordRule {
    override fun check(item: LegacyTenderRecord): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyTenderRecordGraceDaysRangeRule(var min: Int, var max: Int) : LegacyTenderRecordRule {
    override fun check(item: LegacyTenderRecord): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTenderRecordAllowedChecksumRule(var allowed: MutableList<String>) : LegacyTenderRecordRule {
    override fun check(item: LegacyTenderRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTenderRecordCodePrefixRule(var prefix: String) : LegacyTenderRecordRule {
    override fun check(item: LegacyTenderRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy tender record. */
data class LegacyTenderRecordValidator(var rules: MutableList<LegacyTenderRecordRule>) {
    fun validate(item: LegacyTenderRecord): MutableList<String> {
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

    fun requireValid(item: LegacyTenderRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTenderRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTenderRecordValidator(): LegacyTenderRecordValidator {
    return LegacyTenderRecordValidator(
        mutableListOf<LegacyTenderRecordRule>(
            LegacyTenderRecordChecksumLengthRule(3600),
            LegacyTenderRecordTitleLengthRule(120),
            LegacyTenderRecordRequiredChecksumRule("legacy tender record needs an entry"),
            LegacyTenderRecordGraceDaysRangeRule(32, 64),
            LegacyTenderRecordAllowedChecksumRule(mutableListOf<String>()),
            LegacyTenderRecordCodePrefixRule("ACM"),
        ),
    )
}
