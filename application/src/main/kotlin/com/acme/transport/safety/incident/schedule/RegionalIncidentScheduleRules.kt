package com.acme.transport.safety.incident.schedule

import com.acme.transport.kit.text.normalize

/** A rule a regional incident schedule must pass. Check returns its problems, or none. */
interface RegionalIncidentScheduleRule {
    fun check(item: RegionalIncidentSchedule): MutableList<String>
}

data class RegionalIncidentScheduleAllowedNameRule(
    var allowed: MutableList<String>,
) : RegionalIncidentScheduleRule {
    override fun check(item: RegionalIncidentSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is missing")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentScheduleCodeLengthRule(var maxLength: Int) : RegionalIncidentScheduleRule {
    override fun check(item: RegionalIncidentSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentScheduleCurrencyLengthRule(var maxLength: Int) : RegionalIncidentScheduleRule {
    override fun check(item: RegionalIncidentSchedule): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentScheduleRequiredCodeRule(var message: String) : RegionalIncidentScheduleRule {
    override fun check(item: RegionalIncidentSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional incident schedule. */
data class RegionalIncidentScheduleValidator(var rules: MutableList<RegionalIncidentScheduleRule>) {
    fun validate(item: RegionalIncidentSchedule): MutableList<String> {
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

    fun requireValid(item: RegionalIncidentSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalIncidentSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalIncidentScheduleValidator(): RegionalIncidentScheduleValidator {
    return RegionalIncidentScheduleValidator(
        mutableListOf<RegionalIncidentScheduleRule>(
            RegionalIncidentScheduleAllowedNameRule(mutableListOf<String>("primary")),
            RegionalIncidentScheduleCodeLengthRule(86400),
            RegionalIncidentScheduleCurrencyLengthRule(64),
            RegionalIncidentScheduleRequiredCodeRule("regional incident schedule needs a value"),
        ),
    )
}
