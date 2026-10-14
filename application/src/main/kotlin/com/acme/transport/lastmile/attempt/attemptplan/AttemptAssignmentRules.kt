package com.acme.transport.lastmile.attempt.attemptplan

/** A rule a attempt assignment must pass. Check returns its problems, or none. */
interface AttemptAssignmentRule {
    fun check(item: AttemptAssignment): MutableList<String>
}

data class AttemptAssignmentCodePrefixRule(var prefix: String) : AttemptAssignmentRule {
    override fun check(item: AttemptAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AttemptAssignmentTitleLengthRule(var maxLength: Int) : AttemptAssignmentRule {
    override fun check(item: AttemptAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptAssignmentRequiredCodeRule(var message: String) : AttemptAssignmentRule {
    override fun check(item: AttemptAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptAssignmentLoadKgRangeRule(var min: Double, var max: Double) : AttemptAssignmentRule {
    override fun check(item: AttemptAssignment): MutableList<String> {
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

/** Runs every rule of a attempt assignment. */
data class AttemptAssignmentValidator(var rules: MutableList<AttemptAssignmentRule>) {
    fun validate(item: AttemptAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AttemptAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptAssignmentValidator(): AttemptAssignmentValidator {
    return AttemptAssignmentValidator(
        mutableListOf<AttemptAssignmentRule>(
            AttemptAssignmentCodePrefixRule("Q"),
            AttemptAssignmentTitleLengthRule(255),
            AttemptAssignmentRequiredCodeRule("attempt assignment needs a value"),
            AttemptAssignmentLoadKgRangeRule(5.8, 1628.9),
        ),
    )
}
