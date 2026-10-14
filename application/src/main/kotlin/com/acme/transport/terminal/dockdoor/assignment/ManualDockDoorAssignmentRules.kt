package com.acme.transport.terminal.dockdoor.assignment

/** A rule a manual dock door assignment must pass. Check returns its problems, or none. */
interface ManualDockDoorAssignmentRule {
    fun check(item: ManualDockDoorAssignment): MutableList<String>
}

data class ManualDockDoorAssignmentCodePrefixRule(var prefix: String) : ManualDockDoorAssignmentRule {
    override fun check(item: ManualDockDoorAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualDockDoorAssignmentRequiredTitleRule(var message: String) : ManualDockDoorAssignmentRule {
    override fun check(item: ManualDockDoorAssignment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDockDoorAssignmentContactEmailLengthRule(var maxLength: Int) : ManualDockDoorAssignmentRule {
    override fun check(item: ManualDockDoorAssignment): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDockDoorAssignmentNameLengthRule(var maxLength: Int) : ManualDockDoorAssignmentRule {
    override fun check(item: ManualDockDoorAssignment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual dock door assignment. */
data class ManualDockDoorAssignmentValidator(var rules: MutableList<ManualDockDoorAssignmentRule>) {
    fun validate(item: ManualDockDoorAssignment): MutableList<String> {
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

    fun requireValid(item: ManualDockDoorAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDockDoorAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDockDoorAssignmentValidator(): ManualDockDoorAssignmentValidator {
    return ManualDockDoorAssignmentValidator(
        mutableListOf<ManualDockDoorAssignmentRule>(
            ManualDockDoorAssignmentCodePrefixRule("Q"),
            ManualDockDoorAssignmentRequiredTitleRule("manual dock door assignment needs an entry"),
            ManualDockDoorAssignmentContactEmailLengthRule(1000),
            ManualDockDoorAssignmentNameLengthRule(120),
        ),
    )
}
