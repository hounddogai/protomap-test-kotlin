package com.acme.transport.network.node.nodegroup

/** A rule a legacy node assignment must pass. Check returns its problems, or none. */
interface LegacyNodeAssignmentRule {
    fun check(item: LegacyNodeAssignment): MutableList<String>
}

data class LegacyNodeAssignmentLimitRangeRule(var min: Int, var max: Int) : LegacyNodeAssignmentRule {
    override fun check(item: LegacyNodeAssignment): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyNodeAssignmentRequiredCodeRule(var message: String) : LegacyNodeAssignmentRule {
    override fun check(item: LegacyNodeAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyNodeAssignmentRequiredExternalRefRule(var message: String) : LegacyNodeAssignmentRule {
    override fun check(item: LegacyNodeAssignment): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyNodeAssignmentOdometerRangeRule(var min: Int, var max: Int) : LegacyNodeAssignmentRule {
    override fun check(item: LegacyNodeAssignment): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class LegacyNodeAssignmentWindowDaysRangeRule(var min: Int, var max: Int) : LegacyNodeAssignmentRule {
    override fun check(item: LegacyNodeAssignment): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class LegacyNodeAssignmentNameLengthRule(var maxLength: Int) : LegacyNodeAssignmentRule {
    override fun check(item: LegacyNodeAssignment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy node assignment. */
data class LegacyNodeAssignmentValidator(var rules: MutableList<LegacyNodeAssignmentRule>) {
    fun validate(item: LegacyNodeAssignment): MutableList<String> {
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

    fun requireValid(item: LegacyNodeAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyNodeAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyNodeAssignmentValidator(): LegacyNodeAssignmentValidator {
    return LegacyNodeAssignmentValidator(
        mutableListOf<LegacyNodeAssignmentRule>(
            LegacyNodeAssignmentLimitRangeRule(16, 32),
            LegacyNodeAssignmentRequiredCodeRule("legacy node assignment needs an entry"),
            LegacyNodeAssignmentRequiredExternalRefRule("legacy node assignment needs a value"),
            LegacyNodeAssignmentOdometerRangeRule(64, 80),
            LegacyNodeAssignmentWindowDaysRangeRule(255, 255),
            LegacyNodeAssignmentNameLengthRule(1000),
        ),
    )
}
