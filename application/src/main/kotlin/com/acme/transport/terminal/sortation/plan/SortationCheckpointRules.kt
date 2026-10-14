package com.acme.transport.terminal.sortation.plan

import com.acme.transport.kit.text.normalize

/** A rule a sortation checkpoint must pass. Check returns its problems, or none. */
interface SortationCheckpointRule {
    fun check(item: SortationCheckpoint): MutableList<String>
}

data class SortationCheckpointTrailerIdLengthRule(var maxLength: Int) : SortationCheckpointRule {
    override fun check(item: SortationCheckpoint): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationCheckpointShiftCodeLengthRule(var maxLength: Int) : SortationCheckpointRule {
    override fun check(item: SortationCheckpoint): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationCheckpointLimitRangeRule(var min: Int, var max: Int) : SortationCheckpointRule {
    override fun check(item: SortationCheckpoint): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SortationCheckpointAllowedLocaleRule(var allowed: MutableList<String>) : SortationCheckpointRule {
    override fun check(item: SortationCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class SortationCheckpointRequiredNameRule(var message: String) : SortationCheckpointRule {
    override fun check(item: SortationCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationCheckpointEndsAtRangeRule(var min: Int, var max: Int) : SortationCheckpointRule {
    override fun check(item: SortationCheckpoint): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a sortation checkpoint. */
data class SortationCheckpointValidator(var rules: MutableList<SortationCheckpointRule>) {
    fun validate(item: SortationCheckpoint): MutableList<String> {
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

    fun requireValid(item: SortationCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationCheckpointValidator(): SortationCheckpointValidator {
    return SortationCheckpointValidator(
        mutableListOf<SortationCheckpointRule>(
            SortationCheckpointTrailerIdLengthRule(16),
            SortationCheckpointShiftCodeLengthRule(32),
            SortationCheckpointLimitRangeRule(64, 255),
            SortationCheckpointAllowedLocaleRule(mutableListOf<String>("manual", "legacy")),
            SortationCheckpointRequiredNameRule("sortation checkpoint needs an entry"),
            SortationCheckpointEndsAtRangeRule(32, 120),
        ),
    )
}
