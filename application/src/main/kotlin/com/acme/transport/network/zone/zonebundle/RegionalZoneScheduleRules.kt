package com.acme.transport.network.zone.zonebundle

/** A rule a regional zone schedule must pass. Check returns its problems, or none. */
interface RegionalZoneScheduleRule {
    fun check(item: RegionalZoneSchedule): MutableList<String>
}

data class RegionalZoneScheduleCodeLengthRule(var maxLength: Int) : RegionalZoneScheduleRule {
    override fun check(item: RegionalZoneSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalZoneScheduleAmountRangeRule(var min: Double, var max: Double) : RegionalZoneScheduleRule {
    override fun check(item: RegionalZoneSchedule): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class RegionalZoneScheduleMaximumRangeRule(var min: Double, var max: Double) : RegionalZoneScheduleRule {
    override fun check(item: RegionalZoneSchedule): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class RegionalZoneScheduleRequiredTitleRule(var message: String) : RegionalZoneScheduleRule {
    override fun check(item: RegionalZoneSchedule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalZoneScheduleTitleLengthRule(var maxLength: Int) : RegionalZoneScheduleRule {
    override fun check(item: RegionalZoneSchedule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalZoneScheduleRequiredCodeRule(var message: String) : RegionalZoneScheduleRule {
    override fun check(item: RegionalZoneSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional zone schedule. */
data class RegionalZoneScheduleValidator(var rules: MutableList<RegionalZoneScheduleRule>) {
    fun validate(item: RegionalZoneSchedule): MutableList<String> {
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

    fun requireValid(item: RegionalZoneSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalZoneSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalZoneScheduleValidator(): RegionalZoneScheduleValidator {
    return RegionalZoneScheduleValidator(
        mutableListOf<RegionalZoneScheduleRule>(
            RegionalZoneScheduleCodeLengthRule(64),
            RegionalZoneScheduleAmountRangeRule(2.7, 3776.2),
            RegionalZoneScheduleMaximumRangeRule(4.5, 1824.5),
            RegionalZoneScheduleRequiredTitleRule("regional zone schedule needs this field"),
            RegionalZoneScheduleTitleLengthRule(64),
            RegionalZoneScheduleRequiredCodeRule("regional zone schedule needs a value"),
        ),
    )
}
