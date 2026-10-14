package com.acme.transport.terminal.dockdoor.agreement

/** A rule a strict dock door plan must pass. Check returns its problems, or none. */
interface StrictDockDoorPlanRule {
    fun check(item: StrictDockDoorPlan): MutableList<String>
}

data class StrictDockDoorPlanCreatedAtRangeRule(var min: Int, var max: Int) : StrictDockDoorPlanRule {
    override fun check(item: StrictDockDoorPlan): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class StrictDockDoorPlanRequiredLaneRule(var message: String) : StrictDockDoorPlanRule {
    override fun check(item: StrictDockDoorPlan): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDockDoorPlanOwnerIdLengthRule(var maxLength: Int) : StrictDockDoorPlanRule {
    override fun check(item: StrictDockDoorPlan): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDockDoorPlanRequiredCodeRule(var message: String) : StrictDockDoorPlanRule {
    override fun check(item: StrictDockDoorPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDockDoorPlanRatioRangeRule(var min: Double, var max: Double) : StrictDockDoorPlanRule {
    override fun check(item: StrictDockDoorPlan): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictDockDoorPlanRequiredOwnerIdRule(var message: String) : StrictDockDoorPlanRule {
    override fun check(item: StrictDockDoorPlan): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict dock door plan. */
data class StrictDockDoorPlanValidator(var rules: MutableList<StrictDockDoorPlanRule>) {
    fun validate(item: StrictDockDoorPlan): MutableList<String> {
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

    fun requireValid(item: StrictDockDoorPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDockDoorPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDockDoorPlanValidator(): StrictDockDoorPlanValidator {
    return StrictDockDoorPlanValidator(
        mutableListOf<StrictDockDoorPlanRule>(
            StrictDockDoorPlanCreatedAtRangeRule(255, 1000),
            StrictDockDoorPlanRequiredLaneRule("strict dock door plan needs this field"),
            StrictDockDoorPlanOwnerIdLengthRule(1000),
            StrictDockDoorPlanRequiredCodeRule("strict dock door plan needs an entry"),
            StrictDockDoorPlanRatioRangeRule(2.1, 4028.5),
            StrictDockDoorPlanRequiredOwnerIdRule("strict dock door plan needs a value"),
        ),
    )
}
