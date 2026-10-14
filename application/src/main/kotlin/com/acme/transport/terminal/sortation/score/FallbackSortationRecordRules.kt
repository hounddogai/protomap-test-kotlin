package com.acme.transport.terminal.sortation.score

/** A rule a fallback sortation record must pass. Check returns its problems, or none. */
interface FallbackSortationRecordRule {
    fun check(item: FallbackSortationRecord): MutableList<String>
}

data class FallbackSortationRecordRequiredCodeRule(var message: String) : FallbackSortationRecordRule {
    override fun check(item: FallbackSortationRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackSortationRecordNotesLengthRule(var maxLength: Int) : FallbackSortationRecordRule {
    override fun check(item: FallbackSortationRecord): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackSortationRecordDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : FallbackSortationRecordRule {
    override fun check(item: FallbackSortationRecord): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class FallbackSortationRecordLimitRangeRule(var min: Int, var max: Int) : FallbackSortationRecordRule {
    override fun check(item: FallbackSortationRecord): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class FallbackSortationRecordLabelLengthRule(var maxLength: Int) : FallbackSortationRecordRule {
    override fun check(item: FallbackSortationRecord): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackSortationRecordStopCountRangeRule(var min: Int, var max: Int) : FallbackSortationRecordRule {
    override fun check(item: FallbackSortationRecord): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback sortation record. */
data class FallbackSortationRecordValidator(var rules: MutableList<FallbackSortationRecordRule>) {
    fun validate(item: FallbackSortationRecord): MutableList<String> {
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

    fun requireValid(item: FallbackSortationRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackSortationRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackSortationRecordValidator(): FallbackSortationRecordValidator {
    return FallbackSortationRecordValidator(
        mutableListOf<FallbackSortationRecordRule>(
            FallbackSortationRecordRequiredCodeRule("fallback sortation record needs a value"),
            FallbackSortationRecordNotesLengthRule(16),
            FallbackSortationRecordDurationMinutesRangeRule(16, 32),
            FallbackSortationRecordLimitRangeRule(16, 86400),
            FallbackSortationRecordLabelLengthRule(120),
            FallbackSortationRecordStopCountRangeRule(16, 80),
        ),
    )
}
