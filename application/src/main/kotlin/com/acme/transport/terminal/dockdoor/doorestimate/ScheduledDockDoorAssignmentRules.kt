package com.acme.transport.terminal.dockdoor.doorestimate

import com.acme.transport.kit.text.normalize

/** A rule a scheduled dock door assignment must pass. Check returns its problems, or none. */
interface ScheduledDockDoorAssignmentRule {
    fun check(item: ScheduledDockDoorAssignment): MutableList<String>
}

data class ScheduledDockDoorAssignmentPalletsRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledDockDoorAssignmentRule {
    override fun check(item: ScheduledDockDoorAssignment): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class ScheduledDockDoorAssignmentAllowedShiftCodeRule(
    var allowed: MutableList<String>,
) : ScheduledDockDoorAssignmentRule {
    override fun check(item: ScheduledDockDoorAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDockDoorAssignmentRequiredCodeRule(var message: String) : ScheduledDockDoorAssignmentRule {
    override fun check(item: ScheduledDockDoorAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDockDoorAssignmentRequiredShiftCodeRule(
    var message: String,
) : ScheduledDockDoorAssignmentRule {
    override fun check(item: ScheduledDockDoorAssignment): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled dock door assignment. */
data class ScheduledDockDoorAssignmentValidator(var rules: MutableList<ScheduledDockDoorAssignmentRule>) {
    fun validate(item: ScheduledDockDoorAssignment): MutableList<String> {
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

    fun requireValid(item: ScheduledDockDoorAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDockDoorAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDockDoorAssignmentValidator(): ScheduledDockDoorAssignmentValidator {
    return ScheduledDockDoorAssignmentValidator(
        mutableListOf<ScheduledDockDoorAssignmentRule>(
            ScheduledDockDoorAssignmentPalletsRangeRule(16, 80),
            ScheduledDockDoorAssignmentAllowedShiftCodeRule(
                mutableListOf<String>("scheduled", "manual", "legacy"),
            ),
            ScheduledDockDoorAssignmentRequiredCodeRule("scheduled dock door assignment needs a value"),
            ScheduledDockDoorAssignmentRequiredShiftCodeRule("scheduled dock door assignment needs this field"),
        ),
    )
}
