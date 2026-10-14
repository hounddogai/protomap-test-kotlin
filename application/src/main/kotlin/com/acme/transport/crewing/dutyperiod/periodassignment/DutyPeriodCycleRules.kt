package com.acme.transport.crewing.dutyperiod.periodassignment

/** A rule a duty period cycle must pass. Check returns its problems, or none. */
interface DutyPeriodCycleRule {
    fun check(item: DutyPeriodCycle): MutableList<String>
}

data class DutyPeriodCycleRequiredCodeRule(var message: String) : DutyPeriodCycleRule {
    override fun check(item: DutyPeriodCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodCycleRequiredUnitRule(var message: String) : DutyPeriodCycleRule {
    override fun check(item: DutyPeriodCycle): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodCycleDriverIdLengthRule(var maxLength: Int) : DutyPeriodCycleRule {
    override fun check(item: DutyPeriodCycle): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period cycle. */
data class DutyPeriodCycleValidator(var rules: MutableList<DutyPeriodCycleRule>) {
    fun validate(item: DutyPeriodCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DutyPeriodCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodCycleValidator(): DutyPeriodCycleValidator {
    return DutyPeriodCycleValidator(
        mutableListOf<DutyPeriodCycleRule>(
            DutyPeriodCycleRequiredCodeRule("duty period cycle needs this field"),
            DutyPeriodCycleRequiredUnitRule("duty period cycle needs this field"),
            DutyPeriodCycleDriverIdLengthRule(120),
        ),
    )
}
