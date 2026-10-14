package com.acme.transport.crewing.dutyperiod.globalperiodsnapshot

/** A rule a pending duty period plan must pass. Check returns its problems, or none. */
interface PendingDutyPeriodPlanRule {
    fun check(item: PendingDutyPeriodPlan): MutableList<String>
}

data class PendingDutyPeriodPlanRevisionRangeRule(var min: Int, var max: Int) : PendingDutyPeriodPlanRule {
    override fun check(item: PendingDutyPeriodPlan): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class PendingDutyPeriodPlanRequiredNameRule(var message: String) : PendingDutyPeriodPlanRule {
    override fun check(item: PendingDutyPeriodPlan): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDutyPeriodPlanCodeLengthRule(var maxLength: Int) : PendingDutyPeriodPlanRule {
    override fun check(item: PendingDutyPeriodPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending duty period plan. */
data class PendingDutyPeriodPlanValidator(var rules: MutableList<PendingDutyPeriodPlanRule>) {
    fun validate(item: PendingDutyPeriodPlan): MutableList<String> {
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

    fun requireValid(item: PendingDutyPeriodPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDutyPeriodPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDutyPeriodPlanValidator(): PendingDutyPeriodPlanValidator {
    return PendingDutyPeriodPlanValidator(
        mutableListOf<PendingDutyPeriodPlanRule>(
            PendingDutyPeriodPlanRevisionRangeRule(80, 86400),
            PendingDutyPeriodPlanRequiredNameRule("pending duty period plan needs this field"),
            PendingDutyPeriodPlanCodeLengthRule(16),
        ),
    )
}
