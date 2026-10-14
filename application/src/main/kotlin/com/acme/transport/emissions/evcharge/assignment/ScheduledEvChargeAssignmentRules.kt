package com.acme.transport.emissions.evcharge.assignment

/** A rule a scheduled ev charge assignment must pass. Check returns its problems, or none. */
interface ScheduledEvChargeAssignmentRule {
    fun check(item: ScheduledEvChargeAssignment): MutableList<String>
}

data class ScheduledEvChargeAssignmentCodeLengthRule(var maxLength: Int) : ScheduledEvChargeAssignmentRule {
    override fun check(item: ScheduledEvChargeAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledEvChargeAssignmentRequiredCategoryRule(
    var message: String,
) : ScheduledEvChargeAssignmentRule {
    override fun check(item: ScheduledEvChargeAssignment): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledEvChargeAssignmentRequiredCodeRule(var message: String) : ScheduledEvChargeAssignmentRule {
    override fun check(item: ScheduledEvChargeAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledEvChargeAssignmentAmountRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledEvChargeAssignmentRule {
    override fun check(item: ScheduledEvChargeAssignment): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledEvChargeAssignmentCategoryLengthRule(var maxLength: Int) : ScheduledEvChargeAssignmentRule {
    override fun check(item: ScheduledEvChargeAssignment): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledEvChargeAssignmentRequiredLabelRule(var message: String) : ScheduledEvChargeAssignmentRule {
    override fun check(item: ScheduledEvChargeAssignment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled ev charge assignment. */
data class ScheduledEvChargeAssignmentValidator(var rules: MutableList<ScheduledEvChargeAssignmentRule>) {
    fun validate(item: ScheduledEvChargeAssignment): MutableList<String> {
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

    fun requireValid(item: ScheduledEvChargeAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledEvChargeAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledEvChargeAssignmentValidator(): ScheduledEvChargeAssignmentValidator {
    return ScheduledEvChargeAssignmentValidator(
        mutableListOf<ScheduledEvChargeAssignmentRule>(
            ScheduledEvChargeAssignmentCodeLengthRule(120),
            ScheduledEvChargeAssignmentRequiredCategoryRule("scheduled ev charge assignment needs a value"),
            ScheduledEvChargeAssignmentRequiredCodeRule("scheduled ev charge assignment needs this field"),
            ScheduledEvChargeAssignmentAmountRangeRule(0.3, 3992.4),
            ScheduledEvChargeAssignmentCategoryLengthRule(80),
            ScheduledEvChargeAssignmentRequiredLabelRule("scheduled ev charge assignment needs this field"),
        ),
    )
}
