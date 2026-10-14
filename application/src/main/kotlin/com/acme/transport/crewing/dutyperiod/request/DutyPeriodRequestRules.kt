package com.acme.transport.crewing.dutyperiod.request

/** A rule a duty period request must pass. Check returns its problems, or none. */
interface DutyPeriodRequestRule {
    fun check(item: DutyPeriodRequest): MutableList<String>
}

data class DutyPeriodRequestRequiredShiftCodeRule(var message: String) : DutyPeriodRequestRule {
    override fun check(item: DutyPeriodRequest): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodRequestCurrencyLengthRule(var maxLength: Int) : DutyPeriodRequestRule {
    override fun check(item: DutyPeriodRequest): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodRequestRequiredNameRule(var message: String) : DutyPeriodRequestRule {
    override fun check(item: DutyPeriodRequest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period request. */
data class DutyPeriodRequestValidator(var rules: MutableList<DutyPeriodRequestRule>) {
    fun validate(item: DutyPeriodRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DutyPeriodRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodRequestValidator(): DutyPeriodRequestValidator {
    return DutyPeriodRequestValidator(
        mutableListOf<DutyPeriodRequestRule>(
            DutyPeriodRequestRequiredShiftCodeRule("duty period request needs this field"),
            DutyPeriodRequestCurrencyLengthRule(1000),
            DutyPeriodRequestRequiredNameRule("duty period request needs this field"),
        ),
    )
}
