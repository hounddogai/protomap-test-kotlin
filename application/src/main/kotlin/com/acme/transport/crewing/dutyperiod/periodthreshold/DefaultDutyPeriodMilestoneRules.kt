package com.acme.transport.crewing.dutyperiod.periodthreshold

/** A rule a default duty period milestone must pass. Check returns its problems, or none. */
interface DefaultDutyPeriodMilestoneRule {
    fun check(item: DefaultDutyPeriodMilestone): MutableList<String>
}

data class DefaultDutyPeriodMilestoneRequiredCodeRule(var message: String) : DefaultDutyPeriodMilestoneRule {
    override fun check(item: DefaultDutyPeriodMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultDutyPeriodMilestoneRequiredShiftCodeRule(
    var message: String,
) : DefaultDutyPeriodMilestoneRule {
    override fun check(item: DefaultDutyPeriodMilestone): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultDutyPeriodMilestoneCodePrefixRule(var prefix: String) : DefaultDutyPeriodMilestoneRule {
    override fun check(item: DefaultDutyPeriodMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDutyPeriodMilestoneEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : DefaultDutyPeriodMilestoneRule {
    override fun check(item: DefaultDutyPeriodMilestone): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDutyPeriodMilestoneRequiredNameRule(var message: String) : DefaultDutyPeriodMilestoneRule {
    override fun check(item: DefaultDutyPeriodMilestone): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default duty period milestone. */
data class DefaultDutyPeriodMilestoneValidator(var rules: MutableList<DefaultDutyPeriodMilestoneRule>) {
    fun validate(item: DefaultDutyPeriodMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultDutyPeriodMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultDutyPeriodMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultDutyPeriodMilestoneValidator(): DefaultDutyPeriodMilestoneValidator {
    return DefaultDutyPeriodMilestoneValidator(
        mutableListOf<DefaultDutyPeriodMilestoneRule>(
            DefaultDutyPeriodMilestoneRequiredCodeRule("default duty period milestone needs a value"),
            DefaultDutyPeriodMilestoneRequiredShiftCodeRule("default duty period milestone needs an entry"),
            DefaultDutyPeriodMilestoneCodePrefixRule("Q"),
            DefaultDutyPeriodMilestoneEndsAtRangeRule(32, 1000),
            DefaultDutyPeriodMilestoneRequiredNameRule("default duty period milestone needs an entry"),
        ),
    )
}
