package com.acme.transport.terminal.sortation.variance

/** A rule a sortation record must pass. Check returns its problems, or none. */
interface SortationRecordRule {
    fun check(item: SortationRecord): MutableList<String>
}

data class SortationRecordStartsAtRangeRule(var min: Int, var max: Int) : SortationRecordRule {
    override fun check(item: SortationRecord): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class SortationRecordCodePrefixRule(var prefix: String) : SortationRecordRule {
    override fun check(item: SortationRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SortationRecordSequenceRangeRule(var min: Int, var max: Int) : SortationRecordRule {
    override fun check(item: SortationRecord): MutableList<String> {
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

data class SortationRecordCodeLengthRule(var maxLength: Int) : SortationRecordRule {
    override fun check(item: SortationRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationRecordApprovedGuardRule(var limit: Double) : SortationRecordRule {
    override fun check(item: SortationRecord): MutableList<String> {
        if (item.approved && item.fuelLiters > limit) {
            return mutableListOf<String>("fuel liters ${item.fuelLiters} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation record. */
data class SortationRecordValidator(var rules: MutableList<SortationRecordRule>) {
    fun validate(item: SortationRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SortationRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationRecordValidator(): SortationRecordValidator {
    return SortationRecordValidator(
        mutableListOf<SortationRecordRule>(
            SortationRecordStartsAtRangeRule(255, 3600),
            SortationRecordCodePrefixRule("Q"),
            SortationRecordSequenceRangeRule(64, 1000),
            SortationRecordCodeLengthRule(255),
            SortationRecordApprovedGuardRule(7.5),
        ),
    )
}
