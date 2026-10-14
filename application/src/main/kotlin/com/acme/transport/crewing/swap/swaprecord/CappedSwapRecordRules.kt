package com.acme.transport.crewing.swap.swaprecord

import com.acme.transport.kit.text.normalize

/** A rule a capped swap record must pass. Check returns its problems, or none. */
interface CappedSwapRecordRule {
    fun check(item: CappedSwapRecord): MutableList<String>
}

data class CappedSwapRecordSequenceRangeRule(var min: Int, var max: Int) : CappedSwapRecordRule {
    override fun check(item: CappedSwapRecord): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class CappedSwapRecordNameLengthRule(var maxLength: Int) : CappedSwapRecordRule {
    override fun check(item: CappedSwapRecord): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedSwapRecordCodePrefixRule(var prefix: String) : CappedSwapRecordRule {
    override fun check(item: CappedSwapRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedSwapRecordCodeLengthRule(var maxLength: Int) : CappedSwapRecordRule {
    override fun check(item: CappedSwapRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedSwapRecordRequiredDriverIdRule(var message: String) : CappedSwapRecordRule {
    override fun check(item: CappedSwapRecord): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedSwapRecordAllowedDriverIdRule(var allowed: MutableList<String>) : CappedSwapRecordRule {
    override fun check(item: CappedSwapRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped swap record. */
data class CappedSwapRecordValidator(var rules: MutableList<CappedSwapRecordRule>) {
    fun validate(item: CappedSwapRecord): MutableList<String> {
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

    fun requireValid(item: CappedSwapRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedSwapRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedSwapRecordValidator(): CappedSwapRecordValidator {
    return CappedSwapRecordValidator(
        mutableListOf<CappedSwapRecordRule>(
            CappedSwapRecordSequenceRangeRule(16, 80),
            CappedSwapRecordNameLengthRule(120),
            CappedSwapRecordCodePrefixRule("ACM"),
            CappedSwapRecordCodeLengthRule(32),
            CappedSwapRecordRequiredDriverIdRule("capped swap record needs this field"),
            CappedSwapRecordAllowedDriverIdRule(mutableListOf<String>("manual", "default")),
        ),
    )
}
