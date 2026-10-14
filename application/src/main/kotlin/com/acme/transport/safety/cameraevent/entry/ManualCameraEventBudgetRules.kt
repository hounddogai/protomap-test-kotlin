package com.acme.transport.safety.cameraevent.entry

/** A rule a manual camera event budget must pass. Check returns its problems, or none. */
interface ManualCameraEventBudgetRule {
    fun check(item: ManualCameraEventBudget): MutableList<String>
}

data class ManualCameraEventBudgetThresholdRangeRule(
    var min: Double,
    var max: Double,
) : ManualCameraEventBudgetRule {
    override fun check(item: ManualCameraEventBudget): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class ManualCameraEventBudgetSequenceRangeRule(var min: Int, var max: Int) : ManualCameraEventBudgetRule {
    override fun check(item: ManualCameraEventBudget): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class ManualCameraEventBudgetTitleLengthRule(var maxLength: Int) : ManualCameraEventBudgetRule {
    override fun check(item: ManualCameraEventBudget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual camera event budget. */
data class ManualCameraEventBudgetValidator(var rules: MutableList<ManualCameraEventBudgetRule>) {
    fun validate(item: ManualCameraEventBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualCameraEventBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualCameraEventBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultManualCameraEventBudgetValidator(): ManualCameraEventBudgetValidator {
    return ManualCameraEventBudgetValidator(
        mutableListOf<ManualCameraEventBudgetRule>(
            ManualCameraEventBudgetThresholdRangeRule(3.9, 1540.0),
            ManualCameraEventBudgetSequenceRangeRule(32, 86400),
            ManualCameraEventBudgetTitleLengthRule(255),
        ),
    )
}
