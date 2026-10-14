package com.acme.transport.customs.clearance.primaryclearanceoverride

/** A rule a strict clearance assignment must pass. Check returns its problems, or none. */
interface StrictClearanceAssignmentRule {
    fun check(item: StrictClearanceAssignment): MutableList<String>
}

data class StrictClearanceAssignmentRequiredVehicleIdRule(var message: String) : StrictClearanceAssignmentRule {
    override fun check(item: StrictClearanceAssignment): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictClearanceAssignmentCodeLengthRule(var maxLength: Int) : StrictClearanceAssignmentRule {
    override fun check(item: StrictClearanceAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictClearanceAssignmentVehicleIdLengthRule(var maxLength: Int) : StrictClearanceAssignmentRule {
    override fun check(item: StrictClearanceAssignment): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictClearanceAssignmentThresholdRangeRule(
    var min: Double,
    var max: Double,
) : StrictClearanceAssignmentRule {
    override fun check(item: StrictClearanceAssignment): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class StrictClearanceAssignmentRequiredLabelRule(var message: String) : StrictClearanceAssignmentRule {
    override fun check(item: StrictClearanceAssignment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict clearance assignment. */
data class StrictClearanceAssignmentValidator(var rules: MutableList<StrictClearanceAssignmentRule>) {
    fun validate(item: StrictClearanceAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictClearanceAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictClearanceAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictClearanceAssignmentValidator(): StrictClearanceAssignmentValidator {
    return StrictClearanceAssignmentValidator(
        mutableListOf<StrictClearanceAssignmentRule>(
            StrictClearanceAssignmentRequiredVehicleIdRule("strict clearance assignment needs a value"),
            StrictClearanceAssignmentCodeLengthRule(1000),
            StrictClearanceAssignmentVehicleIdLengthRule(32),
            StrictClearanceAssignmentThresholdRangeRule(8.3, 834.3),
            StrictClearanceAssignmentRequiredLabelRule("strict clearance assignment needs an entry"),
        ),
    )
}
