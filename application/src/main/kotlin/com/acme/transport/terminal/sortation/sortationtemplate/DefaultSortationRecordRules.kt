package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.kit.text.normalize

/** A rule a default sortation record must pass. Check returns its problems, or none. */
interface DefaultSortationRecordRule {
    fun check(item: DefaultSortationRecord): MutableList<String>
}

data class DefaultSortationRecordAmountRangeRule(
    var min: Double,
    var max: Double,
) : DefaultSortationRecordRule {
    override fun check(item: DefaultSortationRecord): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultSortationRecordLabelLengthRule(var maxLength: Int) : DefaultSortationRecordRule {
    override fun check(item: DefaultSortationRecord): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultSortationRecordCodePrefixRule(var prefix: String) : DefaultSortationRecordRule {
    override fun check(item: DefaultSortationRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultSortationRecordAllowedCodeRule(
    var allowed: MutableList<String>,
) : DefaultSortationRecordRule {
    override fun check(item: DefaultSortationRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class DefaultSortationRecordCodeLengthRule(var maxLength: Int) : DefaultSortationRecordRule {
    override fun check(item: DefaultSortationRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultSortationRecordRevisionRangeRule(var min: Int, var max: Int) : DefaultSortationRecordRule {
    override fun check(item: DefaultSortationRecord): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default sortation record. */
data class DefaultSortationRecordValidator(var rules: MutableList<DefaultSortationRecordRule>) {
    fun validate(item: DefaultSortationRecord): MutableList<String> {
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

    fun requireValid(item: DefaultSortationRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultSortationRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultSortationRecordValidator(): DefaultSortationRecordValidator {
    return DefaultSortationRecordValidator(
        mutableListOf<DefaultSortationRecordRule>(
            DefaultSortationRecordAmountRangeRule(6.2, 3519.3),
            DefaultSortationRecordLabelLengthRule(120),
            DefaultSortationRecordCodePrefixRule("OPS"),
            DefaultSortationRecordAllowedCodeRule(mutableListOf<String>("pending", "batched")),
            DefaultSortationRecordCodeLengthRule(86400),
            DefaultSortationRecordRevisionRangeRule(80, 3600),
        ),
    )
}
