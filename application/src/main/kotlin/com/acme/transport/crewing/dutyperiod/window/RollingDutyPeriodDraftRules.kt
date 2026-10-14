package com.acme.transport.crewing.dutyperiod.window

/** A rule a rolling duty period draft must pass. Check returns its problems, or none. */
interface RollingDutyPeriodDraftRule {
    fun check(item: RollingDutyPeriodDraft): MutableList<String>
}

data class RollingDutyPeriodDraftRequiredCodeRule(var message: String) : RollingDutyPeriodDraftRule {
    override fun check(item: RollingDutyPeriodDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDutyPeriodDraftCodePrefixRule(var prefix: String) : RollingDutyPeriodDraftRule {
    override fun check(item: RollingDutyPeriodDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingDutyPeriodDraftRequiredLabelRule(var message: String) : RollingDutyPeriodDraftRule {
    override fun check(item: RollingDutyPeriodDraft): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDutyPeriodDraftVersionRangeRule(var min: Int, var max: Int) : RollingDutyPeriodDraftRule {
    override fun check(item: RollingDutyPeriodDraft): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rolling duty period draft. */
data class RollingDutyPeriodDraftValidator(var rules: MutableList<RollingDutyPeriodDraftRule>) {
    fun validate(item: RollingDutyPeriodDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingDutyPeriodDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDutyPeriodDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDutyPeriodDraftValidator(): RollingDutyPeriodDraftValidator {
    return RollingDutyPeriodDraftValidator(
        mutableListOf<RollingDutyPeriodDraftRule>(
            RollingDutyPeriodDraftRequiredCodeRule("rolling duty period draft needs this field"),
            RollingDutyPeriodDraftCodePrefixRule("ACM"),
            RollingDutyPeriodDraftRequiredLabelRule("rolling duty period draft needs a value"),
            RollingDutyPeriodDraftVersionRangeRule(16, 64),
        ),
    )
}
