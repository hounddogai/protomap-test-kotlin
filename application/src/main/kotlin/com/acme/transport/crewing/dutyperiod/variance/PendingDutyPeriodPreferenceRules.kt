package com.acme.transport.crewing.dutyperiod.variance

/** A rule a pending duty period preference must pass. Check returns its problems, or none. */
interface PendingDutyPeriodPreferenceRule {
    fun check(item: PendingDutyPeriodPreference): MutableList<String>
}

data class PendingDutyPeriodPreferenceCodeLengthRule(var maxLength: Int) : PendingDutyPeriodPreferenceRule {
    override fun check(item: PendingDutyPeriodPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDutyPeriodPreferenceLabelLengthRule(var maxLength: Int) : PendingDutyPeriodPreferenceRule {
    override fun check(item: PendingDutyPeriodPreference): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDutyPeriodPreferenceRevisionRangeRule(
    var min: Int,
    var max: Int,
) : PendingDutyPeriodPreferenceRule {
    override fun check(item: PendingDutyPeriodPreference): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending duty period preference. */
data class PendingDutyPeriodPreferenceValidator(var rules: MutableList<PendingDutyPeriodPreferenceRule>) {
    fun validate(item: PendingDutyPeriodPreference): MutableList<String> {
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

    fun requireValid(item: PendingDutyPeriodPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDutyPeriodPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDutyPeriodPreferenceValidator(): PendingDutyPeriodPreferenceValidator {
    return PendingDutyPeriodPreferenceValidator(
        mutableListOf<PendingDutyPeriodPreferenceRule>(
            PendingDutyPeriodPreferenceCodeLengthRule(32),
            PendingDutyPeriodPreferenceLabelLengthRule(32),
            PendingDutyPeriodPreferenceRevisionRangeRule(64, 1000),
        ),
    )
}
