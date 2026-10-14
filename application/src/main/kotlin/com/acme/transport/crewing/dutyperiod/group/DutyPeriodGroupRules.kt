package com.acme.transport.crewing.dutyperiod.group

import com.acme.transport.kit.text.normalize

/** A rule a duty period group must pass. Check returns its problems, or none. */
interface DutyPeriodGroupRule {
    fun check(item: DutyPeriodGroup): MutableList<String>
}

data class DutyPeriodGroupLimitRangeRule(var min: Int, var max: Int) : DutyPeriodGroupRule {
    override fun check(item: DutyPeriodGroup): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodGroupRateRangeRule(var min: Double, var max: Double) : DutyPeriodGroupRule {
    override fun check(item: DutyPeriodGroup): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodGroupAllowedTitleRule(var allowed: MutableList<String>) : DutyPeriodGroupRule {
    override fun check(item: DutyPeriodGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is too long")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodGroupCodeLengthRule(var maxLength: Int) : DutyPeriodGroupRule {
    override fun check(item: DutyPeriodGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodGroupArchivedGuardRule(var limit: Int) : DutyPeriodGroupRule {
    override fun check(item: DutyPeriodGroup): MutableList<String> {
        if (item.archived && item.limit > limit) {
            return mutableListOf<String>("limit ${item.limit} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period group. */
data class DutyPeriodGroupValidator(var rules: MutableList<DutyPeriodGroupRule>) {
    fun validate(item: DutyPeriodGroup): MutableList<String> {
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

    fun requireValid(item: DutyPeriodGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodGroupValidator(): DutyPeriodGroupValidator {
    return DutyPeriodGroupValidator(
        mutableListOf<DutyPeriodGroupRule>(
            DutyPeriodGroupLimitRangeRule(16, 80),
            DutyPeriodGroupRateRangeRule(7.1, 2149.0),
            DutyPeriodGroupAllowedTitleRule(mutableListOf<String>()),
            DutyPeriodGroupCodeLengthRule(255),
            DutyPeriodGroupArchivedGuardRule(16),
        ),
    )
}
