package com.acme.transport.safety.coaching.fallbackcoachingmetric

/** A rule a default coaching assignment must pass. Check returns its problems, or none. */
interface DefaultCoachingAssignmentRule {
    fun check(item: DefaultCoachingAssignment): MutableList<String>
}

data class DefaultCoachingAssignmentCodeLengthRule(var maxLength: Int) : DefaultCoachingAssignmentRule {
    override fun check(item: DefaultCoachingAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCoachingAssignmentScoreRangeRule(
    var min: Double,
    var max: Double,
) : DefaultCoachingAssignmentRule {
    override fun check(item: DefaultCoachingAssignment): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCoachingAssignmentExternalRefLengthRule(var maxLength: Int) : DefaultCoachingAssignmentRule {
    override fun check(item: DefaultCoachingAssignment): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCoachingAssignmentAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : DefaultCoachingAssignmentRule {
    override fun check(item: DefaultCoachingAssignment): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default coaching assignment. */
data class DefaultCoachingAssignmentValidator(var rules: MutableList<DefaultCoachingAssignmentRule>) {
    fun validate(item: DefaultCoachingAssignment): MutableList<String> {
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

    fun requireValid(item: DefaultCoachingAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCoachingAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCoachingAssignmentValidator(): DefaultCoachingAssignmentValidator {
    return DefaultCoachingAssignmentValidator(
        mutableListOf<DefaultCoachingAssignmentRule>(
            DefaultCoachingAssignmentCodeLengthRule(64),
            DefaultCoachingAssignmentScoreRangeRule(7.4, 3717.7),
            DefaultCoachingAssignmentExternalRefLengthRule(1000),
            DefaultCoachingAssignmentAttemptsRangeRule(32, 255),
        ),
    )
}
