package com.acme.transport.crewing.dutyperiod.assignment

/** A rule a duty period policy must pass. Check returns its problems, or none. */
interface DutyPeriodPolicyRule {
    fun check(item: DutyPeriodPolicy): MutableList<String>
}

data class DutyPeriodPolicyRequiredLabelRule(var message: String) : DutyPeriodPolicyRule {
    override fun check(item: DutyPeriodPolicy): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodPolicyCarrierLengthRule(var maxLength: Int) : DutyPeriodPolicyRule {
    override fun check(item: DutyPeriodPolicy): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodPolicyCodePrefixRule(var prefix: String) : DutyPeriodPolicyRule {
    override fun check(item: DutyPeriodPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period policy. */
data class DutyPeriodPolicyValidator(var rules: MutableList<DutyPeriodPolicyRule>) {
    fun validate(item: DutyPeriodPolicy): MutableList<String> {
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

    fun requireValid(item: DutyPeriodPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodPolicyValidator(): DutyPeriodPolicyValidator {
    return DutyPeriodPolicyValidator(
        mutableListOf<DutyPeriodPolicyRule>(
            DutyPeriodPolicyRequiredLabelRule("duty period policy needs an entry"),
            DutyPeriodPolicyCarrierLengthRule(1000),
            DutyPeriodPolicyCodePrefixRule("OPS"),
        ),
    )
}
