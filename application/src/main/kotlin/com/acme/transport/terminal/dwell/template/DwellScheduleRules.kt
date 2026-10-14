package com.acme.transport.terminal.dwell.template

import com.acme.transport.kit.text.normalize

/** A rule a dwell schedule must pass. Check returns its problems, or none. */
interface DwellScheduleRule {
    fun check(item: DwellSchedule): MutableList<String>
}

data class DwellScheduleRequiredCodeRule(var message: String) : DwellScheduleRule {
    override fun check(item: DwellSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellScheduleTagsLimitRule(var maxCount: Int, var banned: MutableList<String>) : DwellScheduleRule {
    override fun check(item: DwellSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class DwellScheduleRequiredCountryRule(var message: String) : DwellScheduleRule {
    override fun check(item: DwellSchedule): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellScheduleAllowedCountryRule(var allowed: MutableList<String>) : DwellScheduleRule {
    override fun check(item: DwellSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class DwellScheduleDurationMinutesRangeRule(var min: Int, var max: Int) : DwellScheduleRule {
    override fun check(item: DwellSchedule): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell schedule. */
data class DwellScheduleValidator(var rules: MutableList<DwellScheduleRule>) {
    fun validate(item: DwellSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DwellSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellScheduleValidator(): DwellScheduleValidator {
    return DwellScheduleValidator(
        mutableListOf<DwellScheduleRule>(
            DwellScheduleRequiredCodeRule("dwell schedule needs an entry"),
            DwellScheduleTagsLimitRule(16, mutableListOf<String>("default")),
            DwellScheduleRequiredCountryRule("dwell schedule needs a value"),
            DwellScheduleAllowedCountryRule(mutableListOf<String>("scheduled", "scheduled", "pending")),
            DwellScheduleDurationMinutesRangeRule(3600, 86400),
        ),
    )
}
