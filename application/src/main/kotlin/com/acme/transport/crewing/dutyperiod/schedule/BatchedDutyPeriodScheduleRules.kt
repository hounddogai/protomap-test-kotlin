package com.acme.transport.crewing.dutyperiod.schedule

/** A rule a batched duty period schedule must pass. Check returns its problems, or none. */
interface BatchedDutyPeriodScheduleRule {
    fun check(item: BatchedDutyPeriodSchedule): MutableList<String>
}

data class BatchedDutyPeriodScheduleBudgetRangeRule(
    var min: Double,
    var max: Double,
) : BatchedDutyPeriodScheduleRule {
    override fun check(item: BatchedDutyPeriodSchedule): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDutyPeriodScheduleCategoryLengthRule(var maxLength: Int) : BatchedDutyPeriodScheduleRule {
    override fun check(item: BatchedDutyPeriodSchedule): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDutyPeriodSchedulePinnedGuardRule(var limit: Double) : BatchedDutyPeriodScheduleRule {
    override fun check(item: BatchedDutyPeriodSchedule): MutableList<String> {
        if (item.pinned && item.budget > limit) {
            return mutableListOf<String>("budget ${item.budget} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDutyPeriodScheduleScoreRangeRule(
    var min: Double,
    var max: Double,
) : BatchedDutyPeriodScheduleRule {
    override fun check(item: BatchedDutyPeriodSchedule): MutableList<String> {
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

data class BatchedDutyPeriodScheduleRequiredCategoryRule(var message: String) : BatchedDutyPeriodScheduleRule {
    override fun check(item: BatchedDutyPeriodSchedule): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched duty period schedule. */
data class BatchedDutyPeriodScheduleValidator(var rules: MutableList<BatchedDutyPeriodScheduleRule>) {
    fun validate(item: BatchedDutyPeriodSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedDutyPeriodSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDutyPeriodSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDutyPeriodScheduleValidator(): BatchedDutyPeriodScheduleValidator {
    return BatchedDutyPeriodScheduleValidator(
        mutableListOf<BatchedDutyPeriodScheduleRule>(
            BatchedDutyPeriodScheduleBudgetRangeRule(2.2, 513.3),
            BatchedDutyPeriodScheduleCategoryLengthRule(80),
            BatchedDutyPeriodSchedulePinnedGuardRule(1.8),
            BatchedDutyPeriodScheduleScoreRangeRule(5.1, 4400.9),
            BatchedDutyPeriodScheduleRequiredCategoryRule("batched duty period schedule needs this field"),
        ),
    )
}
