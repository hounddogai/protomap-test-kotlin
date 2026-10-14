package com.acme.transport.crewing.dutyperiod.metric

/** A rule a lenient duty period segment must pass. Check returns its problems, or none. */
interface LenientDutyPeriodSegmentRule {
    fun check(item: LenientDutyPeriodSegment): MutableList<String>
}

data class LenientDutyPeriodSegmentNotesLengthRule(var maxLength: Int) : LenientDutyPeriodSegmentRule {
    override fun check(item: LenientDutyPeriodSegment): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDutyPeriodSegmentRequiredOwnerIdRule(var message: String) : LenientDutyPeriodSegmentRule {
    override fun check(item: LenientDutyPeriodSegment): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDutyPeriodSegmentExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : LenientDutyPeriodSegmentRule {
    override fun check(item: LenientDutyPeriodSegment): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class LenientDutyPeriodSegmentCodeLengthRule(var maxLength: Int) : LenientDutyPeriodSegmentRule {
    override fun check(item: LenientDutyPeriodSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDutyPeriodSegmentCodePrefixRule(var prefix: String) : LenientDutyPeriodSegmentRule {
    override fun check(item: LenientDutyPeriodSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientDutyPeriodSegmentLabelLengthRule(var maxLength: Int) : LenientDutyPeriodSegmentRule {
    override fun check(item: LenientDutyPeriodSegment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient duty period segment. */
data class LenientDutyPeriodSegmentValidator(var rules: MutableList<LenientDutyPeriodSegmentRule>) {
    fun validate(item: LenientDutyPeriodSegment): MutableList<String> {
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

    fun requireValid(item: LenientDutyPeriodSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDutyPeriodSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDutyPeriodSegmentValidator(): LenientDutyPeriodSegmentValidator {
    return LenientDutyPeriodSegmentValidator(
        mutableListOf<LenientDutyPeriodSegmentRule>(
            LenientDutyPeriodSegmentNotesLengthRule(255),
            LenientDutyPeriodSegmentRequiredOwnerIdRule("lenient duty period segment needs this field"),
            LenientDutyPeriodSegmentExpiresAtRangeRule(80, 3600),
            LenientDutyPeriodSegmentCodeLengthRule(80),
            LenientDutyPeriodSegmentCodePrefixRule("X"),
            LenientDutyPeriodSegmentLabelLengthRule(32),
        ),
    )
}
