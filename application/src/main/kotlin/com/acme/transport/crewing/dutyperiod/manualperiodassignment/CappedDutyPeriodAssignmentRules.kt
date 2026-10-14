package com.acme.transport.crewing.dutyperiod.manualperiodassignment

/** A rule a capped duty period assignment must pass. Check returns its problems, or none. */
interface CappedDutyPeriodAssignmentRule {
    fun check(item: CappedDutyPeriodAssignment): MutableList<String>
}

data class CappedDutyPeriodAssignmentDiscountRangeRule(
    var min: Double,
    var max: Double,
) : CappedDutyPeriodAssignmentRule {
    override fun check(item: CappedDutyPeriodAssignment): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class CappedDutyPeriodAssignmentDriverIdLengthRule(var maxLength: Int) : CappedDutyPeriodAssignmentRule {
    override fun check(item: CappedDutyPeriodAssignment): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedDutyPeriodAssignmentReviewerLengthRule(var maxLength: Int) : CappedDutyPeriodAssignmentRule {
    override fun check(item: CappedDutyPeriodAssignment): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedDutyPeriodAssignmentLimitRangeRule(
    var min: Int,
    var max: Int,
) : CappedDutyPeriodAssignmentRule {
    override fun check(item: CappedDutyPeriodAssignment): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped duty period assignment. */
data class CappedDutyPeriodAssignmentValidator(var rules: MutableList<CappedDutyPeriodAssignmentRule>) {
    fun validate(item: CappedDutyPeriodAssignment): MutableList<String> {
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

    fun requireValid(item: CappedDutyPeriodAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedDutyPeriodAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedDutyPeriodAssignmentValidator(): CappedDutyPeriodAssignmentValidator {
    return CappedDutyPeriodAssignmentValidator(
        mutableListOf<CappedDutyPeriodAssignmentRule>(
            CappedDutyPeriodAssignmentDiscountRangeRule(2.0, 1908.7),
            CappedDutyPeriodAssignmentDriverIdLengthRule(1000),
            CappedDutyPeriodAssignmentReviewerLengthRule(120),
            CappedDutyPeriodAssignmentLimitRangeRule(1000, 86400),
        ),
    )
}
