package com.acme.transport.crewing.dutyperiod.milestone

import com.acme.transport.kit.text.normalize

/** A rule a rolling duty period budget must pass. Check returns its problems, or none. */
interface RollingDutyPeriodBudgetRule {
    fun check(item: RollingDutyPeriodBudget): MutableList<String>
}

data class RollingDutyPeriodBudgetRequiredCodeRule(var message: String) : RollingDutyPeriodBudgetRule {
    override fun check(item: RollingDutyPeriodBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDutyPeriodBudgetPhoneLengthRule(var maxLength: Int) : RollingDutyPeriodBudgetRule {
    override fun check(item: RollingDutyPeriodBudget): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDutyPeriodBudgetAllowedPhoneRule(
    var allowed: MutableList<String>,
) : RollingDutyPeriodBudgetRule {
    override fun check(item: RollingDutyPeriodBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class RollingDutyPeriodBudgetVisibleGuardRule(var limit: Int) : RollingDutyPeriodBudgetRule {
    override fun check(item: RollingDutyPeriodBudget): MutableList<String> {
        if (item.visible && item.attempts > limit) {
            return mutableListOf<String>("attempts ${item.attempts} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class RollingDutyPeriodBudgetCodeLengthRule(var maxLength: Int) : RollingDutyPeriodBudgetRule {
    override fun check(item: RollingDutyPeriodBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling duty period budget. */
data class RollingDutyPeriodBudgetValidator(var rules: MutableList<RollingDutyPeriodBudgetRule>) {
    fun validate(item: RollingDutyPeriodBudget): MutableList<String> {
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

    fun requireValid(item: RollingDutyPeriodBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDutyPeriodBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDutyPeriodBudgetValidator(): RollingDutyPeriodBudgetValidator {
    return RollingDutyPeriodBudgetValidator(
        mutableListOf<RollingDutyPeriodBudgetRule>(
            RollingDutyPeriodBudgetRequiredCodeRule("rolling duty period budget needs an entry"),
            RollingDutyPeriodBudgetPhoneLengthRule(32),
            RollingDutyPeriodBudgetAllowedPhoneRule(mutableListOf<String>("global", "strict", "scheduled")),
            RollingDutyPeriodBudgetVisibleGuardRule(120),
            RollingDutyPeriodBudgetCodeLengthRule(64),
        ),
    )
}
