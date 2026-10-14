package com.acme.transport.crewing.dutyperiod.assignment

/** A rule a duty period target must pass. Check returns its problems, or none. */
interface DutyPeriodTargetRule {
    fun check(item: DutyPeriodTarget): MutableList<String>
}

data class DutyPeriodTargetScoreRangeRule(var min: Double, var max: Double) : DutyPeriodTargetRule {
    override fun check(item: DutyPeriodTarget): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodTargetRequiredShiftCodeRule(var message: String) : DutyPeriodTargetRule {
    override fun check(item: DutyPeriodTarget): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodTargetCodePrefixRule(var prefix: String) : DutyPeriodTargetRule {
    override fun check(item: DutyPeriodTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period target. */
data class DutyPeriodTargetValidator(var rules: MutableList<DutyPeriodTargetRule>) {
    fun validate(item: DutyPeriodTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DutyPeriodTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodTargetValidator(): DutyPeriodTargetValidator {
    return DutyPeriodTargetValidator(
        mutableListOf<DutyPeriodTargetRule>(
            DutyPeriodTargetScoreRangeRule(8.0, 4191.1),
            DutyPeriodTargetRequiredShiftCodeRule("duty period target needs a value"),
            DutyPeriodTargetCodePrefixRule("Q"),
        ),
    )
}
