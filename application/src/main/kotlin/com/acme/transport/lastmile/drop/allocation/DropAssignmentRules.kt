package com.acme.transport.lastmile.drop.allocation

/** A rule a drop assignment must pass. Check returns its problems, or none. */
interface DropAssignmentRule {
    fun check(item: DropAssignment): MutableList<String>
}

data class DropAssignmentCodeLengthRule(var maxLength: Int) : DropAssignmentRule {
    override fun check(item: DropAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropAssignmentRequiredVehicleIdRule(var message: String) : DropAssignmentRule {
    override fun check(item: DropAssignment): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropAssignmentStopCountRangeRule(var min: Int, var max: Int) : DropAssignmentRule {
    override fun check(item: DropAssignment): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DropAssignmentLabelLengthRule(var maxLength: Int) : DropAssignmentRule {
    override fun check(item: DropAssignment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop assignment. */
data class DropAssignmentValidator(var rules: MutableList<DropAssignmentRule>) {
    fun validate(item: DropAssignment): MutableList<String> {
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

    fun requireValid(item: DropAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultDropAssignmentValidator(): DropAssignmentValidator {
    return DropAssignmentValidator(
        mutableListOf<DropAssignmentRule>(
            DropAssignmentCodeLengthRule(1000),
            DropAssignmentRequiredVehicleIdRule("drop assignment needs an entry"),
            DropAssignmentStopCountRangeRule(64, 86400),
            DropAssignmentLabelLengthRule(64),
        ),
    )
}
