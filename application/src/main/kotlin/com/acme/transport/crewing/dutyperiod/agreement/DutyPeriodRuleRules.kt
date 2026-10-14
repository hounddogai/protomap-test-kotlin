package com.acme.transport.crewing.dutyperiod.agreement

/** A rule a duty period rule must pass. Check returns its problems, or none. */
interface DutyPeriodRuleRule {
    fun check(item: DutyPeriodRule): MutableList<String>
}

data class DutyPeriodRuleDurationMinutesRangeRule(var min: Int, var max: Int) : DutyPeriodRuleRule {
    override fun check(item: DutyPeriodRule): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodRuleCodeLengthRule(var maxLength: Int) : DutyPeriodRuleRule {
    override fun check(item: DutyPeriodRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodRuleOdometerRangeRule(var min: Int, var max: Int) : DutyPeriodRuleRule {
    override fun check(item: DutyPeriodRule): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period rule. */
data class DutyPeriodRuleValidator(var rules: MutableList<DutyPeriodRuleRule>) {
    fun validate(item: DutyPeriodRule): MutableList<String> {
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

    fun requireValid(item: DutyPeriodRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodRule>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodRuleValidator(): DutyPeriodRuleValidator {
    return DutyPeriodRuleValidator(
        mutableListOf<DutyPeriodRuleRule>(
            DutyPeriodRuleDurationMinutesRangeRule(64, 64),
            DutyPeriodRuleCodeLengthRule(1000),
            DutyPeriodRuleOdometerRangeRule(80, 1000),
        ),
    )
}
