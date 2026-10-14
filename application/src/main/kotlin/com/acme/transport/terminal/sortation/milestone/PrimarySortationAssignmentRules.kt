package com.acme.transport.terminal.sortation.milestone

/** A rule a primary sortation assignment must pass. Check returns its problems, or none. */
interface PrimarySortationAssignmentRule {
    fun check(item: PrimarySortationAssignment): MutableList<String>
}

data class PrimarySortationAssignmentFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : PrimarySortationAssignmentRule {
    override fun check(item: PrimarySortationAssignment): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimarySortationAssignmentRouteCodeLengthRule(var maxLength: Int) : PrimarySortationAssignmentRule {
    override fun check(item: PrimarySortationAssignment): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimarySortationAssignmentCodePrefixRule(var prefix: String) : PrimarySortationAssignmentRule {
    override fun check(item: PrimarySortationAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimarySortationAssignmentAmountRangeRule(
    var min: Double,
    var max: Double,
) : PrimarySortationAssignmentRule {
    override fun check(item: PrimarySortationAssignment): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class PrimarySortationAssignmentCodeLengthRule(var maxLength: Int) : PrimarySortationAssignmentRule {
    override fun check(item: PrimarySortationAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimarySortationAssignmentRequiredLabelRule(var message: String) : PrimarySortationAssignmentRule {
    override fun check(item: PrimarySortationAssignment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary sortation assignment. */
data class PrimarySortationAssignmentValidator(var rules: MutableList<PrimarySortationAssignmentRule>) {
    fun validate(item: PrimarySortationAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimarySortationAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimarySortationAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimarySortationAssignmentValidator(): PrimarySortationAssignmentValidator {
    return PrimarySortationAssignmentValidator(
        mutableListOf<PrimarySortationAssignmentRule>(
            PrimarySortationAssignmentFuelLitersRangeRule(6.7, 1080.7),
            PrimarySortationAssignmentRouteCodeLengthRule(16),
            PrimarySortationAssignmentCodePrefixRule("ACM"),
            PrimarySortationAssignmentAmountRangeRule(1.8, 1342.8),
            PrimarySortationAssignmentCodeLengthRule(1000),
            PrimarySortationAssignmentRequiredLabelRule("primary sortation assignment needs an entry"),
        ),
    )
}
