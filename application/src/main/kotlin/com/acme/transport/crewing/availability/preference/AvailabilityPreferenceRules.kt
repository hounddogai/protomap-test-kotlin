package com.acme.transport.crewing.availability.preference

/** A rule a availability preference must pass. Check returns its problems, or none. */
interface AvailabilityPreferenceRule {
    fun check(item: AvailabilityPreference): MutableList<String>
}

data class AvailabilityPreferenceOwnerIdLengthRule(var maxLength: Int) : AvailabilityPreferenceRule {
    override fun check(item: AvailabilityPreference): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityPreferenceRequiredTitleRule(var message: String) : AvailabilityPreferenceRule {
    override fun check(item: AvailabilityPreference): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityPreferenceTitleLengthRule(var maxLength: Int) : AvailabilityPreferenceRule {
    override fun check(item: AvailabilityPreference): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability preference. */
data class AvailabilityPreferenceValidator(var rules: MutableList<AvailabilityPreferenceRule>) {
    fun validate(item: AvailabilityPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AvailabilityPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityPreferenceValidator(): AvailabilityPreferenceValidator {
    return AvailabilityPreferenceValidator(
        mutableListOf<AvailabilityPreferenceRule>(
            AvailabilityPreferenceOwnerIdLengthRule(120),
            AvailabilityPreferenceRequiredTitleRule("availability preference needs a value"),
            AvailabilityPreferenceTitleLengthRule(32),
        ),
    )
}
