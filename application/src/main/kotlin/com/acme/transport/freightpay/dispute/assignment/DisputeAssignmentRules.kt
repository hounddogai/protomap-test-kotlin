package com.acme.transport.freightpay.dispute.assignment

/** A rule a dispute assignment must pass. Check returns its problems, or none. */
interface DisputeAssignmentRule {
    fun check(item: DisputeAssignment): MutableList<String>
}

data class DisputeAssignmentRequiredCountryRule(var message: String) : DisputeAssignmentRule {
    override fun check(item: DisputeAssignment): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeAssignmentCodeLengthRule(var maxLength: Int) : DisputeAssignmentRule {
    override fun check(item: DisputeAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeAssignmentMinimumRangeRule(var min: Double, var max: Double) : DisputeAssignmentRule {
    override fun check(item: DisputeAssignment): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a dispute assignment. */
data class DisputeAssignmentValidator(var rules: MutableList<DisputeAssignmentRule>) {
    fun validate(item: DisputeAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DisputeAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeAssignmentValidator(): DisputeAssignmentValidator {
    return DisputeAssignmentValidator(
        mutableListOf<DisputeAssignmentRule>(
            DisputeAssignmentRequiredCountryRule("dispute assignment needs a value"),
            DisputeAssignmentCodeLengthRule(32),
            DisputeAssignmentMinimumRangeRule(1.4, 4327.6),
        ),
    )
}
