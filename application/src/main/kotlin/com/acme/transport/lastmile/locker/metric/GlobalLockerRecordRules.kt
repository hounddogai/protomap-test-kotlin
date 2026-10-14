package com.acme.transport.lastmile.locker.metric

/** A rule a global locker record must pass. Check returns its problems, or none. */
interface GlobalLockerRecordRule {
    fun check(item: GlobalLockerRecord): MutableList<String>
}

data class GlobalLockerRecordRequiredSegmentRule(var message: String) : GlobalLockerRecordRule {
    override fun check(item: GlobalLockerRecord): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalLockerRecordWindowDaysRangeRule(var min: Int, var max: Int) : GlobalLockerRecordRule {
    override fun check(item: GlobalLockerRecord): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalLockerRecordDurationMinutesRangeRule(var min: Int, var max: Int) : GlobalLockerRecordRule {
    override fun check(item: GlobalLockerRecord): MutableList<String> {
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

data class GlobalLockerRecordSegmentLengthRule(var maxLength: Int) : GlobalLockerRecordRule {
    override fun check(item: GlobalLockerRecord): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global locker record. */
data class GlobalLockerRecordValidator(var rules: MutableList<GlobalLockerRecordRule>) {
    fun validate(item: GlobalLockerRecord): MutableList<String> {
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

    fun requireValid(item: GlobalLockerRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalLockerRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalLockerRecordValidator(): GlobalLockerRecordValidator {
    return GlobalLockerRecordValidator(
        mutableListOf<GlobalLockerRecordRule>(
            GlobalLockerRecordRequiredSegmentRule("global locker record needs this field"),
            GlobalLockerRecordWindowDaysRangeRule(255, 86400),
            GlobalLockerRecordDurationMinutesRangeRule(120, 255),
            GlobalLockerRecordSegmentLengthRule(16),
        ),
    )
}
