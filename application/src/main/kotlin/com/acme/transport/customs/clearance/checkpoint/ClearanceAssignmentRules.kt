package com.acme.transport.customs.clearance.checkpoint

/** A rule a clearance assignment must pass. Check returns its problems, or none. */
interface ClearanceAssignmentRule {
    fun check(item: ClearanceAssignment): MutableList<String>
}

data class ClearanceAssignmentApprovedGuardRule(var limit: Int) : ClearanceAssignmentRule {
    override fun check(item: ClearanceAssignment): MutableList<String> {
        if (item.approved && item.endsAt > limit) {
            return mutableListOf<String>("ends at ${item.endsAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceAssignmentThresholdRangeRule(var min: Double, var max: Double) : ClearanceAssignmentRule {
    override fun check(item: ClearanceAssignment): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceAssignmentEndsAtRangeRule(var min: Int, var max: Int) : ClearanceAssignmentRule {
    override fun check(item: ClearanceAssignment): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class ClearanceAssignmentTitleLengthRule(var maxLength: Int) : ClearanceAssignmentRule {
    override fun check(item: ClearanceAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance assignment. */
data class ClearanceAssignmentValidator(var rules: MutableList<ClearanceAssignmentRule>) {
    fun validate(item: ClearanceAssignment): MutableList<String> {
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

    fun requireValid(item: ClearanceAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceAssignmentValidator(): ClearanceAssignmentValidator {
    return ClearanceAssignmentValidator(
        mutableListOf<ClearanceAssignmentRule>(
            ClearanceAssignmentApprovedGuardRule(80),
            ClearanceAssignmentThresholdRangeRule(1.5, 754.8),
            ClearanceAssignmentEndsAtRangeRule(255, 86400),
            ClearanceAssignmentTitleLengthRule(64),
        ),
    )
}
