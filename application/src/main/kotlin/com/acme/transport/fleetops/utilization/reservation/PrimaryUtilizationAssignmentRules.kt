package com.acme.transport.fleetops.utilization.reservation

/** A rule a primary utilization assignment must pass. Check returns its problems, or none. */
interface PrimaryUtilizationAssignmentRule {
    fun check(item: PrimaryUtilizationAssignment): MutableList<String>
}

data class PrimaryUtilizationAssignmentRequiredCategoryRule(
    var message: String,
) : PrimaryUtilizationAssignmentRule {
    override fun check(item: PrimaryUtilizationAssignment): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryUtilizationAssignmentRequiredTitleRule(
    var message: String,
) : PrimaryUtilizationAssignmentRule {
    override fun check(item: PrimaryUtilizationAssignment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryUtilizationAssignmentLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryUtilizationAssignmentRule {
    override fun check(item: PrimaryUtilizationAssignment): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a primary utilization assignment. */
data class PrimaryUtilizationAssignmentValidator(var rules: MutableList<PrimaryUtilizationAssignmentRule>) {
    fun validate(item: PrimaryUtilizationAssignment): MutableList<String> {
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

    fun requireValid(item: PrimaryUtilizationAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryUtilizationAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryUtilizationAssignmentValidator(): PrimaryUtilizationAssignmentValidator {
    return PrimaryUtilizationAssignmentValidator(
        mutableListOf<PrimaryUtilizationAssignmentRule>(
            PrimaryUtilizationAssignmentRequiredCategoryRule("primary utilization assignment needs an entry"),
            PrimaryUtilizationAssignmentRequiredTitleRule("primary utilization assignment needs an entry"),
            PrimaryUtilizationAssignmentLoadKgRangeRule(2.6, 1625.9),
        ),
    )
}
