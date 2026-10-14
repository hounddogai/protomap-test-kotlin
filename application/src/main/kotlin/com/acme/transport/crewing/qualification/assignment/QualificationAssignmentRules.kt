package com.acme.transport.crewing.qualification.assignment

/** A rule a qualification assignment must pass. Check returns its problems, or none. */
interface QualificationAssignmentRule {
    fun check(item: QualificationAssignment): MutableList<String>
}

data class QualificationAssignmentRequiredCodeRule(var message: String) : QualificationAssignmentRule {
    override fun check(item: QualificationAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationAssignmentCodePrefixRule(var prefix: String) : QualificationAssignmentRule {
    override fun check(item: QualificationAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class QualificationAssignmentWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : QualificationAssignmentRule {
    override fun check(item: QualificationAssignment): MutableList<String> {
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

data class QualificationAssignmentRequiredTitleRule(var message: String) : QualificationAssignmentRule {
    override fun check(item: QualificationAssignment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationAssignmentCodeLengthRule(var maxLength: Int) : QualificationAssignmentRule {
    override fun check(item: QualificationAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationAssignmentCapacityRangeRule(var min: Int, var max: Int) : QualificationAssignmentRule {
    override fun check(item: QualificationAssignment): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification assignment. */
data class QualificationAssignmentValidator(var rules: MutableList<QualificationAssignmentRule>) {
    fun validate(item: QualificationAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: QualificationAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationAssignmentValidator(): QualificationAssignmentValidator {
    return QualificationAssignmentValidator(
        mutableListOf<QualificationAssignmentRule>(
            QualificationAssignmentRequiredCodeRule("qualification assignment needs a value"),
            QualificationAssignmentCodePrefixRule("X"),
            QualificationAssignmentWindowDaysRangeRule(32, 86400),
            QualificationAssignmentRequiredTitleRule("qualification assignment needs this field"),
            QualificationAssignmentCodeLengthRule(80),
            QualificationAssignmentCapacityRangeRule(32, 120),
        ),
    )
}
