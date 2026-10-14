package com.acme.transport.linehaul.trailer.budget

/** A rule a rolling trailer assignment must pass. Check returns its problems, or none. */
interface RollingTrailerAssignmentRule {
    fun check(item: RollingTrailerAssignment): MutableList<String>
}

data class RollingTrailerAssignmentThresholdRangeRule(
    var min: Double,
    var max: Double,
) : RollingTrailerAssignmentRule {
    override fun check(item: RollingTrailerAssignment): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerAssignmentOdometerRangeRule(
    var min: Int,
    var max: Int,
) : RollingTrailerAssignmentRule {
    override fun check(item: RollingTrailerAssignment): MutableList<String> {
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

data class RollingTrailerAssignmentPhoneLengthRule(var maxLength: Int) : RollingTrailerAssignmentRule {
    override fun check(item: RollingTrailerAssignment): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerAssignmentCodeLengthRule(var maxLength: Int) : RollingTrailerAssignmentRule {
    override fun check(item: RollingTrailerAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerAssignmentCeilingRangeRule(
    var min: Double,
    var max: Double,
) : RollingTrailerAssignmentRule {
    override fun check(item: RollingTrailerAssignment): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rolling trailer assignment. */
data class RollingTrailerAssignmentValidator(var rules: MutableList<RollingTrailerAssignmentRule>) {
    fun validate(item: RollingTrailerAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingTrailerAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingTrailerAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingTrailerAssignmentValidator(): RollingTrailerAssignmentValidator {
    return RollingTrailerAssignmentValidator(
        mutableListOf<RollingTrailerAssignmentRule>(
            RollingTrailerAssignmentThresholdRangeRule(4.7, 2368.4),
            RollingTrailerAssignmentOdometerRangeRule(120, 86400),
            RollingTrailerAssignmentPhoneLengthRule(1000),
            RollingTrailerAssignmentCodeLengthRule(16),
            RollingTrailerAssignmentCeilingRangeRule(3.4, 1525.3),
        ),
    )
}
