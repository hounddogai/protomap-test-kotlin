package com.acme.transport.crewing.restbreak.regionalbreakpreference

/** A rule a fallback rest break assignment must pass. Check returns its problems, or none. */
interface FallbackRestBreakAssignmentRule {
    fun check(item: FallbackRestBreakAssignment): MutableList<String>
}

data class FallbackRestBreakAssignmentReviewerLengthRule(var maxLength: Int) : FallbackRestBreakAssignmentRule {
    override fun check(item: FallbackRestBreakAssignment): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRestBreakAssignmentMinimumRangeRule(
    var min: Double,
    var max: Double,
) : FallbackRestBreakAssignmentRule {
    override fun check(item: FallbackRestBreakAssignment): MutableList<String> {
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

data class FallbackRestBreakAssignmentRequiredReviewerRule(
    var message: String,
) : FallbackRestBreakAssignmentRule {
    override fun check(item: FallbackRestBreakAssignment): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRestBreakAssignmentCodeLengthRule(var maxLength: Int) : FallbackRestBreakAssignmentRule {
    override fun check(item: FallbackRestBreakAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback rest break assignment. */
data class FallbackRestBreakAssignmentValidator(var rules: MutableList<FallbackRestBreakAssignmentRule>) {
    fun validate(item: FallbackRestBreakAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackRestBreakAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRestBreakAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRestBreakAssignmentValidator(): FallbackRestBreakAssignmentValidator {
    return FallbackRestBreakAssignmentValidator(
        mutableListOf<FallbackRestBreakAssignmentRule>(
            FallbackRestBreakAssignmentReviewerLengthRule(32),
            FallbackRestBreakAssignmentMinimumRangeRule(2.1, 1697.9),
            FallbackRestBreakAssignmentRequiredReviewerRule("fallback rest break assignment needs this field"),
            FallbackRestBreakAssignmentCodeLengthRule(32),
        ),
    )
}
