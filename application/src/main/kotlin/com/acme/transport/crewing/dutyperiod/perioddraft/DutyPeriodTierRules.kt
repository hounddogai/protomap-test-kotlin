package com.acme.transport.crewing.dutyperiod.perioddraft

/** A rule a duty period tier must pass. Check returns its problems, or none. */
interface DutyPeriodTierRule {
    fun check(item: DutyPeriodTier): MutableList<String>
}

data class DutyPeriodTierRequiredCodeRule(var message: String) : DutyPeriodTierRule {
    override fun check(item: DutyPeriodTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodTierEtaMinutesRangeRule(var min: Int, var max: Int) : DutyPeriodTierRule {
    override fun check(item: DutyPeriodTier): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodTierRequiredChannelRule(var message: String) : DutyPeriodTierRule {
    override fun check(item: DutyPeriodTier): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodTierLabelLengthRule(var maxLength: Int) : DutyPeriodTierRule {
    override fun check(item: DutyPeriodTier): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodTierCodeLengthRule(var maxLength: Int) : DutyPeriodTierRule {
    override fun check(item: DutyPeriodTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period tier. */
data class DutyPeriodTierValidator(var rules: MutableList<DutyPeriodTierRule>) {
    fun validate(item: DutyPeriodTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DutyPeriodTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodTier>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodTierValidator(): DutyPeriodTierValidator {
    return DutyPeriodTierValidator(
        mutableListOf<DutyPeriodTierRule>(
            DutyPeriodTierRequiredCodeRule("duty period tier needs a value"),
            DutyPeriodTierEtaMinutesRangeRule(80, 120),
            DutyPeriodTierRequiredChannelRule("duty period tier needs a value"),
            DutyPeriodTierLabelLengthRule(3600),
            DutyPeriodTierCodeLengthRule(64),
        ),
    )
}
