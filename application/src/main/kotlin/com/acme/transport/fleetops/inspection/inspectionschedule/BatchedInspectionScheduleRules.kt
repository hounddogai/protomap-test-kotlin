package com.acme.transport.fleetops.inspection.inspectionschedule

/** A rule a batched inspection schedule must pass. Check returns its problems, or none. */
interface BatchedInspectionScheduleRule {
    fun check(item: BatchedInspectionSchedule): MutableList<String>
}

data class BatchedInspectionScheduleLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : BatchedInspectionScheduleRule {
    override fun check(item: BatchedInspectionSchedule): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedInspectionScheduleCodeLengthRule(var maxLength: Int) : BatchedInspectionScheduleRule {
    override fun check(item: BatchedInspectionSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedInspectionScheduleTitleLengthRule(var maxLength: Int) : BatchedInspectionScheduleRule {
    override fun check(item: BatchedInspectionSchedule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched inspection schedule. */
data class BatchedInspectionScheduleValidator(var rules: MutableList<BatchedInspectionScheduleRule>) {
    fun validate(item: BatchedInspectionSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedInspectionSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedInspectionSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedInspectionScheduleValidator(): BatchedInspectionScheduleValidator {
    return BatchedInspectionScheduleValidator(
        mutableListOf<BatchedInspectionScheduleRule>(
            BatchedInspectionScheduleLoadKgRangeRule(2.0, 460.5),
            BatchedInspectionScheduleCodeLengthRule(120),
            BatchedInspectionScheduleTitleLengthRule(120),
        ),
    )
}
