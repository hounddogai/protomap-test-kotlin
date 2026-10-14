package com.acme.transport.safety.incident.claim

import com.acme.transport.kit.text.normalize

/** A rule a incident schedule must pass. Check returns its problems, or none. */
interface IncidentScheduleRule {
    fun check(item: IncidentSchedule): MutableList<String>
}

data class IncidentScheduleRequiredCodeRule(var message: String) : IncidentScheduleRule {
    override fun check(item: IncidentSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentScheduleCodeLengthRule(var maxLength: Int) : IncidentScheduleRule {
    override fun check(item: IncidentSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentScheduleRequiredVehicleIdRule(var message: String) : IncidentScheduleRule {
    override fun check(item: IncidentSchedule): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentScheduleAllowedTitleRule(var allowed: MutableList<String>) : IncidentScheduleRule {
    override fun check(item: IncidentSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident schedule. */
data class IncidentScheduleValidator(var rules: MutableList<IncidentScheduleRule>) {
    fun validate(item: IncidentSchedule): MutableList<String> {
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

    fun requireValid(item: IncidentSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentScheduleValidator(): IncidentScheduleValidator {
    return IncidentScheduleValidator(
        mutableListOf<IncidentScheduleRule>(
            IncidentScheduleRequiredCodeRule("incident schedule needs an entry"),
            IncidentScheduleCodeLengthRule(86400),
            IncidentScheduleRequiredVehicleIdRule("incident schedule needs this field"),
            IncidentScheduleAllowedTitleRule(mutableListOf<String>("primary")),
        ),
    )
}
