package com.acme.transport.crewing.dutyperiod.periodreview

/** A rule a pending duty period exception must pass. Check returns its problems, or none. */
interface PendingDutyPeriodExceptionRule {
    fun check(item: PendingDutyPeriodException): MutableList<String>
}

data class PendingDutyPeriodExceptionMaximumRangeRule(
    var min: Double,
    var max: Double,
) : PendingDutyPeriodExceptionRule {
    override fun check(item: PendingDutyPeriodException): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingDutyPeriodExceptionCodeLengthRule(var maxLength: Int) : PendingDutyPeriodExceptionRule {
    override fun check(item: PendingDutyPeriodException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDutyPeriodExceptionRequiredLabelRule(var message: String) : PendingDutyPeriodExceptionRule {
    override fun check(item: PendingDutyPeriodException): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDutyPeriodExceptionRequiredShiftCodeRule(
    var message: String,
) : PendingDutyPeriodExceptionRule {
    override fun check(item: PendingDutyPeriodException): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDutyPeriodExceptionTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : PendingDutyPeriodExceptionRule {
    override fun check(item: PendingDutyPeriodException): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class PendingDutyPeriodExceptionWeightRangeRule(
    var min: Double,
    var max: Double,
) : PendingDutyPeriodExceptionRule {
    override fun check(item: PendingDutyPeriodException): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending duty period exception. */
data class PendingDutyPeriodExceptionValidator(var rules: MutableList<PendingDutyPeriodExceptionRule>) {
    fun validate(item: PendingDutyPeriodException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingDutyPeriodException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDutyPeriodException>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDutyPeriodExceptionValidator(): PendingDutyPeriodExceptionValidator {
    return PendingDutyPeriodExceptionValidator(
        mutableListOf<PendingDutyPeriodExceptionRule>(
            PendingDutyPeriodExceptionMaximumRangeRule(3.8, 3047.1),
            PendingDutyPeriodExceptionCodeLengthRule(255),
            PendingDutyPeriodExceptionRequiredLabelRule("pending duty period exception needs this field"),
            PendingDutyPeriodExceptionRequiredShiftCodeRule("pending duty period exception needs this field"),
            PendingDutyPeriodExceptionTagsLimitRule(
                3600,
                mutableListOf<String>("default", "pending", "weighted"),
            ),
            PendingDutyPeriodExceptionWeightRangeRule(3.9, 4700.3),
        ),
    )
}
