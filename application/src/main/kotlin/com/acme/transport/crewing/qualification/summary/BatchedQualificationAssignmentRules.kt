package com.acme.transport.crewing.qualification.summary

/** A rule a batched qualification assignment must pass. Check returns its problems, or none. */
interface BatchedQualificationAssignmentRule {
    fun check(item: BatchedQualificationAssignment): MutableList<String>
}

data class BatchedQualificationAssignmentRequiredTimezoneRule(
    var message: String,
) : BatchedQualificationAssignmentRule {
    override fun check(item: BatchedQualificationAssignment): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationAssignmentExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : BatchedQualificationAssignmentRule {
    override fun check(item: BatchedQualificationAssignment): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationAssignmentCodeLengthRule(
    var maxLength: Int,
) : BatchedQualificationAssignmentRule {
    override fun check(item: BatchedQualificationAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationAssignmentNameLengthRule(
    var maxLength: Int,
) : BatchedQualificationAssignmentRule {
    override fun check(item: BatchedQualificationAssignment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationAssignmentDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : BatchedQualificationAssignmentRule {
    override fun check(item: BatchedQualificationAssignment): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched qualification assignment. */
data class BatchedQualificationAssignmentValidator(var rules: MutableList<BatchedQualificationAssignmentRule>) {
    fun validate(item: BatchedQualificationAssignment): MutableList<String> {
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

    fun requireValid(item: BatchedQualificationAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedQualificationAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedQualificationAssignmentValidator(): BatchedQualificationAssignmentValidator {
    return BatchedQualificationAssignmentValidator(
        mutableListOf<BatchedQualificationAssignmentRule>(
            BatchedQualificationAssignmentRequiredTimezoneRule(
                "batched qualification assignment needs this field",
            ),
            BatchedQualificationAssignmentExpiresAtRangeRule(32, 80),
            BatchedQualificationAssignmentCodeLengthRule(16),
            BatchedQualificationAssignmentNameLengthRule(120),
            BatchedQualificationAssignmentDistanceKmRangeRule(6.8, 4999.1),
        ),
    )
}
