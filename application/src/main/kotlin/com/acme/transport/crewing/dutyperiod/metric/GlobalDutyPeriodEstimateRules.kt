package com.acme.transport.crewing.dutyperiod.metric

import com.acme.transport.kit.text.normalize

/** A rule a global duty period estimate must pass. Check returns its problems, or none. */
interface GlobalDutyPeriodEstimateRule {
    fun check(item: GlobalDutyPeriodEstimate): MutableList<String>
}

data class GlobalDutyPeriodEstimateMaximumRangeRule(
    var min: Double,
    var max: Double,
) : GlobalDutyPeriodEstimateRule {
    override fun check(item: GlobalDutyPeriodEstimate): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDutyPeriodEstimatePhoneLengthRule(var maxLength: Int) : GlobalDutyPeriodEstimateRule {
    override fun check(item: GlobalDutyPeriodEstimate): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDutyPeriodEstimateAllowedPhoneRule(
    var allowed: MutableList<String>,
) : GlobalDutyPeriodEstimateRule {
    override fun check(item: GlobalDutyPeriodEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDutyPeriodEstimateRequiredNotesRule(var message: String) : GlobalDutyPeriodEstimateRule {
    override fun check(item: GlobalDutyPeriodEstimate): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalDutyPeriodEstimateNotesLengthRule(var maxLength: Int) : GlobalDutyPeriodEstimateRule {
    override fun check(item: GlobalDutyPeriodEstimate): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDutyPeriodEstimateTitleLengthRule(var maxLength: Int) : GlobalDutyPeriodEstimateRule {
    override fun check(item: GlobalDutyPeriodEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global duty period estimate. */
data class GlobalDutyPeriodEstimateValidator(var rules: MutableList<GlobalDutyPeriodEstimateRule>) {
    fun validate(item: GlobalDutyPeriodEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalDutyPeriodEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalDutyPeriodEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalDutyPeriodEstimateValidator(): GlobalDutyPeriodEstimateValidator {
    return GlobalDutyPeriodEstimateValidator(
        mutableListOf<GlobalDutyPeriodEstimateRule>(
            GlobalDutyPeriodEstimateMaximumRangeRule(9.5, 1957.7),
            GlobalDutyPeriodEstimatePhoneLengthRule(86400),
            GlobalDutyPeriodEstimateAllowedPhoneRule(mutableListOf<String>("legacy", "manual", "default")),
            GlobalDutyPeriodEstimateRequiredNotesRule("global duty period estimate needs a value"),
            GlobalDutyPeriodEstimateNotesLengthRule(3600),
            GlobalDutyPeriodEstimateTitleLengthRule(86400),
        ),
    )
}
