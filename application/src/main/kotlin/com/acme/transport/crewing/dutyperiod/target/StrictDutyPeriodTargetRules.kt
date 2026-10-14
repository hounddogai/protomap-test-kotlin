package com.acme.transport.crewing.dutyperiod.target

import com.acme.transport.kit.text.normalize

/** A rule a strict duty period target must pass. Check returns its problems, or none. */
interface StrictDutyPeriodTargetRule {
    fun check(item: StrictDutyPeriodTarget): MutableList<String>
}

data class StrictDutyPeriodTargetNameLengthRule(var maxLength: Int) : StrictDutyPeriodTargetRule {
    override fun check(item: StrictDutyPeriodTarget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDutyPeriodTargetRetriesRangeRule(var min: Int, var max: Int) : StrictDutyPeriodTargetRule {
    override fun check(item: StrictDutyPeriodTarget): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictDutyPeriodTargetCreatedAtRangeRule(var min: Int, var max: Int) : StrictDutyPeriodTargetRule {
    override fun check(item: StrictDutyPeriodTarget): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class StrictDutyPeriodTargetAllowedDriverIdRule(
    var allowed: MutableList<String>,
) : StrictDutyPeriodTargetRule {
    override fun check(item: StrictDutyPeriodTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict duty period target. */
data class StrictDutyPeriodTargetValidator(var rules: MutableList<StrictDutyPeriodTargetRule>) {
    fun validate(item: StrictDutyPeriodTarget): MutableList<String> {
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

    fun requireValid(item: StrictDutyPeriodTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDutyPeriodTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDutyPeriodTargetValidator(): StrictDutyPeriodTargetValidator {
    return StrictDutyPeriodTargetValidator(
        mutableListOf<StrictDutyPeriodTargetRule>(
            StrictDutyPeriodTargetNameLengthRule(120),
            StrictDutyPeriodTargetRetriesRangeRule(64, 80),
            StrictDutyPeriodTargetCreatedAtRangeRule(64, 120),
            StrictDutyPeriodTargetAllowedDriverIdRule(mutableListOf<String>()),
        ),
    )
}
