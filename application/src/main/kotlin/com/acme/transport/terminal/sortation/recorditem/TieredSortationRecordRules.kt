package com.acme.transport.terminal.sortation.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a tiered sortation record must pass. Check returns its problems, or none. */
interface TieredSortationRecordRule {
    fun check(item: TieredSortationRecord): MutableList<String>
}

data class TieredSortationRecordCodeLengthRule(var maxLength: Int) : TieredSortationRecordRule {
    override fun check(item: TieredSortationRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationRecordCodePrefixRule(var prefix: String) : TieredSortationRecordRule {
    override fun check(item: TieredSortationRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationRecordRequiredTitleRule(var message: String) : TieredSortationRecordRule {
    override fun check(item: TieredSortationRecord): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationRecordFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : TieredSortationRecordRule {
    override fun check(item: TieredSortationRecord): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class TieredSortationRecordAllowedCodeRule(var allowed: MutableList<String>) : TieredSortationRecordRule {
    override fun check(item: TieredSortationRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered sortation record. */
data class TieredSortationRecordValidator(var rules: MutableList<TieredSortationRecordRule>) {
    fun validate(item: TieredSortationRecord): MutableList<String> {
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

    fun requireValid(item: TieredSortationRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredSortationRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredSortationRecordValidator(): TieredSortationRecordValidator {
    return TieredSortationRecordValidator(
        mutableListOf<TieredSortationRecordRule>(
            TieredSortationRecordCodeLengthRule(255),
            TieredSortationRecordCodePrefixRule("REF"),
            TieredSortationRecordRequiredTitleRule("tiered sortation record needs an entry"),
            TieredSortationRecordFloorValueRangeRule(9.3, 3446.3),
            TieredSortationRecordAllowedCodeRule(mutableListOf<String>("lenient")),
        ),
    )
}
