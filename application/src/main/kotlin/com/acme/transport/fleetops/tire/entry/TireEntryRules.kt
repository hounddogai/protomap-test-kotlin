package com.acme.transport.fleetops.tire.entry

import com.acme.transport.kit.text.normalize

/** A rule a tire entry must pass. Check returns its problems, or none. */
interface TireEntryRule {
    fun check(item: TireEntry): MutableList<String>
}

data class TireEntryAllowedLabelRule(var allowed: MutableList<String>) : TireEntryRule {
    override fun check(item: TireEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is stale")
        }
        return mutableListOf<String>()
    }
}

data class TireEntryRequiredReviewerRule(var message: String) : TireEntryRule {
    override fun check(item: TireEntry): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireEntryShiftCodeLengthRule(var maxLength: Int) : TireEntryRule {
    override fun check(item: TireEntry): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireEntryLabelLengthRule(var maxLength: Int) : TireEntryRule {
    override fun check(item: TireEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireEntryCodeLengthRule(var maxLength: Int) : TireEntryRule {
    override fun check(item: TireEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireEntryQuantityRangeRule(var min: Int, var max: Int) : TireEntryRule {
    override fun check(item: TireEntry): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tire entry. */
data class TireEntryValidator(var rules: MutableList<TireEntryRule>) {
    fun validate(item: TireEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultTireEntryValidator(): TireEntryValidator {
    return TireEntryValidator(
        mutableListOf<TireEntryRule>(
            TireEntryAllowedLabelRule(mutableListOf<String>()),
            TireEntryRequiredReviewerRule("tire entry needs this field"),
            TireEntryShiftCodeLengthRule(255),
            TireEntryLabelLengthRule(32),
            TireEntryCodeLengthRule(64),
            TireEntryQuantityRangeRule(80, 120),
        ),
    )
}
