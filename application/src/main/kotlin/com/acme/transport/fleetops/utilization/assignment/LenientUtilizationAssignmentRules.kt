package com.acme.transport.fleetops.utilization.assignment

/** A rule a lenient utilization assignment must pass. Check returns its problems, or none. */
interface LenientUtilizationAssignmentRule {
    fun check(item: LenientUtilizationAssignment): MutableList<String>
}

data class LenientUtilizationAssignmentVehicleIdLengthRule(
    var maxLength: Int,
) : LenientUtilizationAssignmentRule {
    override fun check(item: LenientUtilizationAssignment): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientUtilizationAssignmentRequiredLabelRule(
    var message: String,
) : LenientUtilizationAssignmentRule {
    override fun check(item: LenientUtilizationAssignment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientUtilizationAssignmentMinimumRangeRule(
    var min: Double,
    var max: Double,
) : LenientUtilizationAssignmentRule {
    override fun check(item: LenientUtilizationAssignment): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientUtilizationAssignmentUnitLengthRule(var maxLength: Int) : LenientUtilizationAssignmentRule {
    override fun check(item: LenientUtilizationAssignment): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientUtilizationAssignmentRevisionRangeRule(
    var min: Int,
    var max: Int,
) : LenientUtilizationAssignmentRule {
    override fun check(item: LenientUtilizationAssignment): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientUtilizationAssignmentLabelLengthRule(var maxLength: Int) : LenientUtilizationAssignmentRule {
    override fun check(item: LenientUtilizationAssignment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient utilization assignment. */
data class LenientUtilizationAssignmentValidator(var rules: MutableList<LenientUtilizationAssignmentRule>) {
    fun validate(item: LenientUtilizationAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientUtilizationAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientUtilizationAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientUtilizationAssignmentValidator(): LenientUtilizationAssignmentValidator {
    return LenientUtilizationAssignmentValidator(
        mutableListOf<LenientUtilizationAssignmentRule>(
            LenientUtilizationAssignmentVehicleIdLengthRule(80),
            LenientUtilizationAssignmentRequiredLabelRule("lenient utilization assignment needs an entry"),
            LenientUtilizationAssignmentMinimumRangeRule(5.3, 212.9),
            LenientUtilizationAssignmentUnitLengthRule(120),
            LenientUtilizationAssignmentRevisionRangeRule(32, 32),
            LenientUtilizationAssignmentLabelLengthRule(3600),
        ),
    )
}
