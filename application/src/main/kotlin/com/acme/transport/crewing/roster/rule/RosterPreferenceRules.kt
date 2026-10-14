package com.acme.transport.crewing.roster.rule

/** A rule a roster preference must pass. Check returns its problems, or none. */
interface RosterPreferenceRule {
    fun check(item: RosterPreference): MutableList<String>
}

data class RosterPreferenceRequiredTitleRule(var message: String) : RosterPreferenceRule {
    override fun check(item: RosterPreference): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterPreferenceExpiresAtRangeRule(var min: Int, var max: Int) : RosterPreferenceRule {
    override fun check(item: RosterPreference): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RosterPreferenceCodePrefixRule(var prefix: String) : RosterPreferenceRule {
    override fun check(item: RosterPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RosterPreferenceTitleLengthRule(var maxLength: Int) : RosterPreferenceRule {
    override fun check(item: RosterPreference): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster preference. */
data class RosterPreferenceValidator(var rules: MutableList<RosterPreferenceRule>) {
    fun validate(item: RosterPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterPreferenceValidator(): RosterPreferenceValidator {
    return RosterPreferenceValidator(
        mutableListOf<RosterPreferenceRule>(
            RosterPreferenceRequiredTitleRule("roster preference needs an entry"),
            RosterPreferenceExpiresAtRangeRule(80, 86400),
            RosterPreferenceCodePrefixRule("ACM"),
            RosterPreferenceTitleLengthRule(255),
        ),
    )
}
