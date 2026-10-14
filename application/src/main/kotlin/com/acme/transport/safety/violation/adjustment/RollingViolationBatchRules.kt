package com.acme.transport.safety.violation.adjustment

/** A rule a rolling violation batch must pass. Check returns its problems, or none. */
interface RollingViolationBatchRule {
    fun check(item: RollingViolationBatch): MutableList<String>
}

data class RollingViolationBatchStartsAtRangeRule(var min: Int, var max: Int) : RollingViolationBatchRule {
    override fun check(item: RollingViolationBatch): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class RollingViolationBatchScoreRangeRule(var min: Double, var max: Double) : RollingViolationBatchRule {
    override fun check(item: RollingViolationBatch): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class RollingViolationBatchLabelLengthRule(var maxLength: Int) : RollingViolationBatchRule {
    override fun check(item: RollingViolationBatch): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingViolationBatchRouteCodeLengthRule(var maxLength: Int) : RollingViolationBatchRule {
    override fun check(item: RollingViolationBatch): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling violation batch. */
data class RollingViolationBatchValidator(var rules: MutableList<RollingViolationBatchRule>) {
    fun validate(item: RollingViolationBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingViolationBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingViolationBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingViolationBatchValidator(): RollingViolationBatchValidator {
    return RollingViolationBatchValidator(
        mutableListOf<RollingViolationBatchRule>(
            RollingViolationBatchStartsAtRangeRule(80, 1000),
            RollingViolationBatchScoreRangeRule(10.0, 1591.8),
            RollingViolationBatchLabelLengthRule(3600),
            RollingViolationBatchRouteCodeLengthRule(86400),
        ),
    )
}
