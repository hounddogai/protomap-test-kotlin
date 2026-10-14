package com.acme.transport.terminal.sortation.sortationsnapshot

import com.acme.transport.kit.text.normalize

/** A rule a legacy sortation snapshot must pass. Check returns its problems, or none. */
interface LegacySortationSnapshotRule {
    fun check(item: LegacySortationSnapshot): MutableList<String>
}

data class LegacySortationSnapshotCapacityRangeRule(var min: Int, var max: Int) : LegacySortationSnapshotRule {
    override fun check(item: LegacySortationSnapshot): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class LegacySortationSnapshotTimezoneLengthRule(var maxLength: Int) : LegacySortationSnapshotRule {
    override fun check(item: LegacySortationSnapshot): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacySortationSnapshotRequiredTimezoneRule(var message: String) : LegacySortationSnapshotRule {
    override fun check(item: LegacySortationSnapshot): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacySortationSnapshotAllowedCurrencyRule(
    var allowed: MutableList<String>,
) : LegacySortationSnapshotRule {
    override fun check(item: LegacySortationSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.currency))) {
            return mutableListOf<String>("currency ${item.currency} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class LegacySortationSnapshotLabelLengthRule(var maxLength: Int) : LegacySortationSnapshotRule {
    override fun check(item: LegacySortationSnapshot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacySortationSnapshotRequiredCodeRule(var message: String) : LegacySortationSnapshotRule {
    override fun check(item: LegacySortationSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy sortation snapshot. */
data class LegacySortationSnapshotValidator(var rules: MutableList<LegacySortationSnapshotRule>) {
    fun validate(item: LegacySortationSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacySortationSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacySortationSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacySortationSnapshotValidator(): LegacySortationSnapshotValidator {
    return LegacySortationSnapshotValidator(
        mutableListOf<LegacySortationSnapshotRule>(
            LegacySortationSnapshotCapacityRangeRule(80, 86400),
            LegacySortationSnapshotTimezoneLengthRule(80),
            LegacySortationSnapshotRequiredTimezoneRule("legacy sortation snapshot needs a value"),
            LegacySortationSnapshotAllowedCurrencyRule(mutableListOf<String>("strict", "rolling")),
            LegacySortationSnapshotLabelLengthRule(255),
            LegacySortationSnapshotRequiredCodeRule("legacy sortation snapshot needs an entry"),
        ),
    )
}
