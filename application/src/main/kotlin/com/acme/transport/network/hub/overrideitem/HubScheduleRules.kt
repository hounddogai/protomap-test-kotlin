package com.acme.transport.network.hub.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a hub schedule must pass. Check returns its problems, or none. */
interface HubScheduleRule {
    fun check(item: HubSchedule): MutableList<String>
}

data class HubScheduleRequiredCodeRule(var message: String) : HubScheduleRule {
    override fun check(item: HubSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubScheduleMinimumRangeRule(var min: Double, var max: Double) : HubScheduleRule {
    override fun check(item: HubSchedule): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HubScheduleAllowedTimezoneRule(var allowed: MutableList<String>) : HubScheduleRule {
    override fun check(item: HubSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is too long")
        }
        return mutableListOf<String>()
    }
}

data class HubScheduleCreatedAtRangeRule(var min: Int, var max: Int) : HubScheduleRule {
    override fun check(item: HubSchedule): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HubScheduleRequiredTimezoneRule(var message: String) : HubScheduleRule {
    override fun check(item: HubSchedule): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub schedule. */
data class HubScheduleValidator(var rules: MutableList<HubScheduleRule>) {
    fun validate(item: HubSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HubSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultHubScheduleValidator(): HubScheduleValidator {
    return HubScheduleValidator(
        mutableListOf<HubScheduleRule>(
            HubScheduleRequiredCodeRule("hub schedule needs an entry"),
            HubScheduleMinimumRangeRule(6.0, 859.8),
            HubScheduleAllowedTimezoneRule(mutableListOf<String>("lenient", "seasonal", "global")),
            HubScheduleCreatedAtRangeRule(80, 120),
            HubScheduleRequiredTimezoneRule("hub schedule needs an entry"),
        ),
    )
}
