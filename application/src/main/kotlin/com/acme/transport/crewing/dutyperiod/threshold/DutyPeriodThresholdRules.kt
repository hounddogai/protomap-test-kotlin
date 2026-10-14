package com.acme.transport.crewing.dutyperiod.threshold

/** A rule a duty period threshold must pass. Check returns its problems, or none. */
interface DutyPeriodThresholdRule {
    fun check(item: DutyPeriodThreshold): MutableList<String>
}

data class DutyPeriodThresholdRequiredLabelRule(var message: String) : DutyPeriodThresholdRule {
    override fun check(item: DutyPeriodThreshold): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodThresholdEtaMinutesRangeRule(var min: Int, var max: Int) : DutyPeriodThresholdRule {
    override fun check(item: DutyPeriodThreshold): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodThresholdCodePrefixRule(var prefix: String) : DutyPeriodThresholdRule {
    override fun check(item: DutyPeriodThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period threshold. */
data class DutyPeriodThresholdValidator(var rules: MutableList<DutyPeriodThresholdRule>) {
    fun validate(item: DutyPeriodThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DutyPeriodThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodThresholdValidator(): DutyPeriodThresholdValidator {
    return DutyPeriodThresholdValidator(
        mutableListOf<DutyPeriodThresholdRule>(
            DutyPeriodThresholdRequiredLabelRule("duty period threshold needs this field"),
            DutyPeriodThresholdEtaMinutesRangeRule(120, 3600),
            DutyPeriodThresholdCodePrefixRule("OPS"),
        ),
    )
}
