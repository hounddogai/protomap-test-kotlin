package com.acme.transport.terminal.dockdoor.agreement

import com.acme.transport.kit.text.normalize

/** A rule a dock door schedule must pass. Check returns its problems, or none. */
interface DockDoorScheduleRule {
    fun check(item: DockDoorSchedule): MutableList<String>
}

data class DockDoorScheduleAllowedNameRule(var allowed: MutableList<String>) : DockDoorScheduleRule {
    override fun check(item: DockDoorSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorScheduleRequiredNameRule(var message: String) : DockDoorScheduleRule {
    override fun check(item: DockDoorSchedule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorScheduleRequiredSummaryRule(var message: String) : DockDoorScheduleRule {
    override fun check(item: DockDoorSchedule): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door schedule. */
data class DockDoorScheduleValidator(var rules: MutableList<DockDoorScheduleRule>) {
    fun validate(item: DockDoorSchedule): MutableList<String> {
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

    fun requireValid(item: DockDoorSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorScheduleValidator(): DockDoorScheduleValidator {
    return DockDoorScheduleValidator(
        mutableListOf<DockDoorScheduleRule>(
            DockDoorScheduleAllowedNameRule(mutableListOf<String>("default")),
            DockDoorScheduleRequiredNameRule("dock door schedule needs this field"),
            DockDoorScheduleRequiredSummaryRule("dock door schedule needs this field"),
        ),
    )
}
