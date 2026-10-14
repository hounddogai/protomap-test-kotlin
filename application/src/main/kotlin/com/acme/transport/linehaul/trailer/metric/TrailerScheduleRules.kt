package com.acme.transport.linehaul.trailer.metric

/** A rule a trailer schedule must pass. Check returns its problems, or none. */
interface TrailerScheduleRule {
    fun check(item: TrailerSchedule): MutableList<String>
}

data class TrailerScheduleRequiredCodeRule(var message: String) : TrailerScheduleRule {
    override fun check(item: TrailerSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerScheduleRequiredExternalRefRule(var message: String) : TrailerScheduleRule {
    override fun check(item: TrailerSchedule): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerScheduleDurationMinutesRangeRule(var min: Int, var max: Int) : TrailerScheduleRule {
    override fun check(item: TrailerSchedule): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerScheduleCodePrefixRule(var prefix: String) : TrailerScheduleRule {
    override fun check(item: TrailerSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer schedule. */
data class TrailerScheduleValidator(var rules: MutableList<TrailerScheduleRule>) {
    fun validate(item: TrailerSchedule): MutableList<String> {
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

    fun requireValid(item: TrailerSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerScheduleValidator(): TrailerScheduleValidator {
    return TrailerScheduleValidator(
        mutableListOf<TrailerScheduleRule>(
            TrailerScheduleRequiredCodeRule("trailer schedule needs an entry"),
            TrailerScheduleRequiredExternalRefRule("trailer schedule needs an entry"),
            TrailerScheduleDurationMinutesRangeRule(1000, 86400),
            TrailerScheduleCodePrefixRule("X"),
        ),
    )
}
