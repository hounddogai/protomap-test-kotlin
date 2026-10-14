package com.acme.transport.lastmile.parcel.schedule

import com.acme.transport.kit.text.normalize

/** A rule a parcel schedule must pass. Check returns its problems, or none. */
interface ParcelScheduleRule {
    fun check(item: ParcelSchedule): MutableList<String>
}

data class ParcelScheduleFloorValueRangeRule(var min: Double, var max: Double) : ParcelScheduleRule {
    override fun check(item: ParcelSchedule): MutableList<String> {
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

data class ParcelScheduleAllowedTitleRule(var allowed: MutableList<String>) : ParcelScheduleRule {
    override fun check(item: ParcelSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class ParcelScheduleTitleLengthRule(var maxLength: Int) : ParcelScheduleRule {
    override fun check(item: ParcelSchedule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelScheduleRequiredChecksumRule(var message: String) : ParcelScheduleRule {
    override fun check(item: ParcelSchedule): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelScheduleRequiredTitleRule(var message: String) : ParcelScheduleRule {
    override fun check(item: ParcelSchedule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel schedule. */
data class ParcelScheduleValidator(var rules: MutableList<ParcelScheduleRule>) {
    fun validate(item: ParcelSchedule): MutableList<String> {
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

    fun requireValid(item: ParcelSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelScheduleValidator(): ParcelScheduleValidator {
    return ParcelScheduleValidator(
        mutableListOf<ParcelScheduleRule>(
            ParcelScheduleFloorValueRangeRule(8.0, 3480.8),
            ParcelScheduleAllowedTitleRule(mutableListOf<String>("manual", "rolling", "primary")),
            ParcelScheduleTitleLengthRule(120),
            ParcelScheduleRequiredChecksumRule("parcel schedule needs an entry"),
            ParcelScheduleRequiredTitleRule("parcel schedule needs a value"),
        ),
    )
}
