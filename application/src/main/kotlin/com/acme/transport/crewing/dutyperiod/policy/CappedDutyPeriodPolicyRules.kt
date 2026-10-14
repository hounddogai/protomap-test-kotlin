package com.acme.transport.crewing.dutyperiod.policy

/** A rule a capped duty period policy must pass. Check returns its problems, or none. */
interface CappedDutyPeriodPolicyRule {
    fun check(item: CappedDutyPeriodPolicy): MutableList<String>
}

data class CappedDutyPeriodPolicyOdometerRangeRule(var min: Int, var max: Int) : CappedDutyPeriodPolicyRule {
    override fun check(item: CappedDutyPeriodPolicy): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class CappedDutyPeriodPolicyRequiredContactEmailRule(var message: String) : CappedDutyPeriodPolicyRule {
    override fun check(item: CappedDutyPeriodPolicy): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDutyPeriodPolicyPriorityRangeRule(var min: Int, var max: Int) : CappedDutyPeriodPolicyRule {
    override fun check(item: CappedDutyPeriodPolicy): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped duty period policy. */
data class CappedDutyPeriodPolicyValidator(var rules: MutableList<CappedDutyPeriodPolicyRule>) {
    fun validate(item: CappedDutyPeriodPolicy): MutableList<String> {
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

    fun requireValid(item: CappedDutyPeriodPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedDutyPeriodPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedDutyPeriodPolicyValidator(): CappedDutyPeriodPolicyValidator {
    return CappedDutyPeriodPolicyValidator(
        mutableListOf<CappedDutyPeriodPolicyRule>(
            CappedDutyPeriodPolicyOdometerRangeRule(255, 86400),
            CappedDutyPeriodPolicyRequiredContactEmailRule("capped duty period policy needs a value"),
            CappedDutyPeriodPolicyPriorityRangeRule(32, 255),
        ),
    )
}
