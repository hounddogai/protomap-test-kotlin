package com.acme.transport.crewing.qualification.checkpoint

/** A rule a pending qualification assignment must pass. Check returns its problems, or none. */
interface PendingQualificationAssignmentRule {
    fun check(item: PendingQualificationAssignment): MutableList<String>
}

data class PendingQualificationAssignmentLabelLengthRule(
    var maxLength: Int,
) : PendingQualificationAssignmentRule {
    override fun check(item: PendingQualificationAssignment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingQualificationAssignmentRatioRangeRule(
    var min: Double,
    var max: Double,
) : PendingQualificationAssignmentRule {
    override fun check(item: PendingQualificationAssignment): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingQualificationAssignmentRequiredLabelRule(
    var message: String,
) : PendingQualificationAssignmentRule {
    override fun check(item: PendingQualificationAssignment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending qualification assignment. */
data class PendingQualificationAssignmentValidator(var rules: MutableList<PendingQualificationAssignmentRule>) {
    fun validate(item: PendingQualificationAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingQualificationAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingQualificationAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingQualificationAssignmentValidator(): PendingQualificationAssignmentValidator {
    return PendingQualificationAssignmentValidator(
        mutableListOf<PendingQualificationAssignmentRule>(
            PendingQualificationAssignmentLabelLengthRule(80),
            PendingQualificationAssignmentRatioRangeRule(1.9, 2398.5),
            PendingQualificationAssignmentRequiredLabelRule("pending qualification assignment needs an entry"),
        ),
    )
}
