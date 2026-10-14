package com.acme.transport.crewing.dutyperiod.assignment

import com.acme.transport.kit.text.normalize

/** A rule a duty period rollup must pass. Check returns its problems, or none. */
interface DutyPeriodRollupRule {
    fun check(item: DutyPeriodRollup): MutableList<String>
}

data class DutyPeriodRollupRequiredDepotRule(var message: String) : DutyPeriodRollupRule {
    override fun check(item: DutyPeriodRollup): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodRollupAllowedLaneRule(var allowed: MutableList<String>) : DutyPeriodRollupRule {
    override fun check(item: DutyPeriodRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodRollupUpdatedAtRangeRule(var min: Int, var max: Int) : DutyPeriodRollupRule {
    override fun check(item: DutyPeriodRollup): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodRollupWindowDaysRangeRule(var min: Int, var max: Int) : DutyPeriodRollupRule {
    override fun check(item: DutyPeriodRollup): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class DutyPeriodRollupCodeLengthRule(var maxLength: Int) : DutyPeriodRollupRule {
    override fun check(item: DutyPeriodRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period rollup. */
data class DutyPeriodRollupValidator(var rules: MutableList<DutyPeriodRollupRule>) {
    fun validate(item: DutyPeriodRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DutyPeriodRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodRollupValidator(): DutyPeriodRollupValidator {
    return DutyPeriodRollupValidator(
        mutableListOf<DutyPeriodRollupRule>(
            DutyPeriodRollupRequiredDepotRule("duty period rollup needs an entry"),
            DutyPeriodRollupAllowedLaneRule(mutableListOf<String>("regional", "manual")),
            DutyPeriodRollupUpdatedAtRangeRule(80, 120),
            DutyPeriodRollupWindowDaysRangeRule(16, 32),
            DutyPeriodRollupCodeLengthRule(80),
        ),
    )
}
