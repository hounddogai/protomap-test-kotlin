package com.acme.transport.terminal.dockdoor.assignment

/** A rule a fallback dock door assignment must pass. Check returns its problems, or none. */
interface FallbackDockDoorAssignmentRule {
    fun check(item: FallbackDockDoorAssignment): MutableList<String>
}

data class FallbackDockDoorAssignmentRequiredCodeRule(var message: String) : FallbackDockDoorAssignmentRule {
    override fun check(item: FallbackDockDoorAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDockDoorAssignmentPriorityRangeRule(
    var min: Int,
    var max: Int,
) : FallbackDockDoorAssignmentRule {
    override fun check(item: FallbackDockDoorAssignment): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class FallbackDockDoorAssignmentDriverIdLengthRule(var maxLength: Int) : FallbackDockDoorAssignmentRule {
    override fun check(item: FallbackDockDoorAssignment): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDockDoorAssignmentRequiredDriverIdRule(
    var message: String,
) : FallbackDockDoorAssignmentRule {
    override fun check(item: FallbackDockDoorAssignment): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback dock door assignment. */
data class FallbackDockDoorAssignmentValidator(var rules: MutableList<FallbackDockDoorAssignmentRule>) {
    fun validate(item: FallbackDockDoorAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackDockDoorAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDockDoorAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDockDoorAssignmentValidator(): FallbackDockDoorAssignmentValidator {
    return FallbackDockDoorAssignmentValidator(
        mutableListOf<FallbackDockDoorAssignmentRule>(
            FallbackDockDoorAssignmentRequiredCodeRule("fallback dock door assignment needs a value"),
            FallbackDockDoorAssignmentPriorityRangeRule(80, 3600),
            FallbackDockDoorAssignmentDriverIdLengthRule(255),
            FallbackDockDoorAssignmentRequiredDriverIdRule("fallback dock door assignment needs this field"),
        ),
    )
}
