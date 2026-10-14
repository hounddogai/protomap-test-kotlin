package com.acme.transport.network.cutoff.scheduledcutoffclaim

/** A rule a scheduled cutoff preference must pass. Check returns its problems, or none. */
interface ScheduledCutoffPreferenceRule {
    fun check(item: ScheduledCutoffPreference): MutableList<String>
}

data class ScheduledCutoffPreferenceRequiredLocaleRule(var message: String) : ScheduledCutoffPreferenceRule {
    override fun check(item: ScheduledCutoffPreference): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCutoffPreferenceRequiredTitleRule(var message: String) : ScheduledCutoffPreferenceRule {
    override fun check(item: ScheduledCutoffPreference): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCutoffPreferenceDescriptionLengthRule(var maxLength: Int) : ScheduledCutoffPreferenceRule {
    override fun check(item: ScheduledCutoffPreference): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled cutoff preference. */
data class ScheduledCutoffPreferenceValidator(var rules: MutableList<ScheduledCutoffPreferenceRule>) {
    fun validate(item: ScheduledCutoffPreference): MutableList<String> {
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

    fun requireValid(item: ScheduledCutoffPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledCutoffPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledCutoffPreferenceValidator(): ScheduledCutoffPreferenceValidator {
    return ScheduledCutoffPreferenceValidator(
        mutableListOf<ScheduledCutoffPreferenceRule>(
            ScheduledCutoffPreferenceRequiredLocaleRule("scheduled cutoff preference needs an entry"),
            ScheduledCutoffPreferenceRequiredTitleRule("scheduled cutoff preference needs a value"),
            ScheduledCutoffPreferenceDescriptionLengthRule(80),
        ),
    )
}
