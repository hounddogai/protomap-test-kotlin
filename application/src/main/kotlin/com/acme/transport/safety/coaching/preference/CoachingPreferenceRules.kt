package com.acme.transport.safety.coaching.preference

/** A rule a coaching preference must pass. Check returns its problems, or none. */
interface CoachingPreferenceRule {
    fun check(item: CoachingPreference): MutableList<String>
}

data class CoachingPreferenceRequiredChannelRule(var message: String) : CoachingPreferenceRule {
    override fun check(item: CoachingPreference): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingPreferenceTitleLengthRule(var maxLength: Int) : CoachingPreferenceRule {
    override fun check(item: CoachingPreference): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingPreferenceRevisionRangeRule(var min: Int, var max: Int) : CoachingPreferenceRule {
    override fun check(item: CoachingPreference): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching preference. */
data class CoachingPreferenceValidator(var rules: MutableList<CoachingPreferenceRule>) {
    fun validate(item: CoachingPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CoachingPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingPreferenceValidator(): CoachingPreferenceValidator {
    return CoachingPreferenceValidator(
        mutableListOf<CoachingPreferenceRule>(
            CoachingPreferenceRequiredChannelRule("coaching preference needs a value"),
            CoachingPreferenceTitleLengthRule(1000),
            CoachingPreferenceRevisionRangeRule(16, 32),
        ),
    )
}
