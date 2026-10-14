package com.acme.transport.safety.violation.allocation

/** A rule a lenient violation assignment must pass. Check returns its problems, or none. */
interface LenientViolationAssignmentRule {
    fun check(item: LenientViolationAssignment): MutableList<String>
}

data class LenientViolationAssignmentCodePrefixRule(var prefix: String) : LenientViolationAssignmentRule {
    override fun check(item: LenientViolationAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientViolationAssignmentRequiredCodeRule(var message: String) : LenientViolationAssignmentRule {
    override fun check(item: LenientViolationAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientViolationAssignmentSequenceRangeRule(
    var min: Int,
    var max: Int,
) : LenientViolationAssignmentRule {
    override fun check(item: LenientViolationAssignment): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientViolationAssignmentCodeLengthRule(var maxLength: Int) : LenientViolationAssignmentRule {
    override fun check(item: LenientViolationAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientViolationAssignmentDiscountRangeRule(
    var min: Double,
    var max: Double,
) : LenientViolationAssignmentRule {
    override fun check(item: LenientViolationAssignment): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient violation assignment. */
data class LenientViolationAssignmentValidator(var rules: MutableList<LenientViolationAssignmentRule>) {
    fun validate(item: LenientViolationAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientViolationAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientViolationAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientViolationAssignmentValidator(): LenientViolationAssignmentValidator {
    return LenientViolationAssignmentValidator(
        mutableListOf<LenientViolationAssignmentRule>(
            LenientViolationAssignmentCodePrefixRule("Q"),
            LenientViolationAssignmentRequiredCodeRule("lenient violation assignment needs an entry"),
            LenientViolationAssignmentSequenceRangeRule(255, 3600),
            LenientViolationAssignmentCodeLengthRule(64),
            LenientViolationAssignmentDiscountRangeRule(0.8, 4108.5),
        ),
    )
}
