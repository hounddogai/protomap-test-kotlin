package com.acme.transport.terminal.sortation.baseline

/** A rule a legacy sortation record must pass. Check returns its problems, or none. */
interface LegacySortationRecordRule {
    fun check(item: LegacySortationRecord): MutableList<String>
}

data class LegacySortationRecordCodePrefixRule(var prefix: String) : LegacySortationRecordRule {
    override fun check(item: LegacySortationRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacySortationRecordRetriesRangeRule(var min: Int, var max: Int) : LegacySortationRecordRule {
    override fun check(item: LegacySortationRecord): MutableList<String> {
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

data class LegacySortationRecordRequiredCategoryRule(var message: String) : LegacySortationRecordRule {
    override fun check(item: LegacySortationRecord): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacySortationRecordCategoryLengthRule(var maxLength: Int) : LegacySortationRecordRule {
    override fun check(item: LegacySortationRecord): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy sortation record. */
data class LegacySortationRecordValidator(var rules: MutableList<LegacySortationRecordRule>) {
    fun validate(item: LegacySortationRecord): MutableList<String> {
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

    fun requireValid(item: LegacySortationRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacySortationRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacySortationRecordValidator(): LegacySortationRecordValidator {
    return LegacySortationRecordValidator(
        mutableListOf<LegacySortationRecordRule>(
            LegacySortationRecordCodePrefixRule("REF"),
            LegacySortationRecordRetriesRangeRule(16, 255),
            LegacySortationRecordRequiredCategoryRule("legacy sortation record needs this field"),
            LegacySortationRecordCategoryLengthRule(3600),
        ),
    )
}
