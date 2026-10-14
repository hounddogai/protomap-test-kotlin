package com.acme.transport.terminal.dockdoor.dooragreement

/** A rule a dock door assignment must pass. Check returns its problems, or none. */
interface DockDoorAssignmentRule {
    fun check(item: DockDoorAssignment): MutableList<String>
}

data class DockDoorAssignmentRequiredLabelRule(var message: String) : DockDoorAssignmentRule {
    override fun check(item: DockDoorAssignment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorAssignmentNameLengthRule(var maxLength: Int) : DockDoorAssignmentRule {
    override fun check(item: DockDoorAssignment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorAssignmentUpdatedAtRangeRule(var min: Int, var max: Int) : DockDoorAssignmentRule {
    override fun check(item: DockDoorAssignment): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a dock door assignment. */
data class DockDoorAssignmentValidator(var rules: MutableList<DockDoorAssignmentRule>) {
    fun validate(item: DockDoorAssignment): MutableList<String> {
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

    fun requireValid(item: DockDoorAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorAssignmentValidator(): DockDoorAssignmentValidator {
    return DockDoorAssignmentValidator(
        mutableListOf<DockDoorAssignmentRule>(
            DockDoorAssignmentRequiredLabelRule("dock door assignment needs a value"),
            DockDoorAssignmentNameLengthRule(255),
            DockDoorAssignmentUpdatedAtRangeRule(80, 255),
        ),
    )
}
