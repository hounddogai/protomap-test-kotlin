package com.acme.transport.crewing.roster.preference

/** A rule a default roster preference must pass. Check returns its problems, or none. */
interface DefaultRosterPreferenceRule {
    fun check(item: DefaultRosterPreference): MutableList<String>
}

data class DefaultRosterPreferenceRequiredCategoryRule(var message: String) : DefaultRosterPreferenceRule {
    override fun check(item: DefaultRosterPreference): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRosterPreferencePhoneLengthRule(var maxLength: Int) : DefaultRosterPreferenceRule {
    override fun check(item: DefaultRosterPreference): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRosterPreferenceCodePrefixRule(var prefix: String) : DefaultRosterPreferenceRule {
    override fun check(item: DefaultRosterPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRosterPreferenceRequiredTitleRule(var message: String) : DefaultRosterPreferenceRule {
    override fun check(item: DefaultRosterPreference): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default roster preference. */
data class DefaultRosterPreferenceValidator(var rules: MutableList<DefaultRosterPreferenceRule>) {
    fun validate(item: DefaultRosterPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultRosterPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRosterPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRosterPreferenceValidator(): DefaultRosterPreferenceValidator {
    return DefaultRosterPreferenceValidator(
        mutableListOf<DefaultRosterPreferenceRule>(
            DefaultRosterPreferenceRequiredCategoryRule("default roster preference needs an entry"),
            DefaultRosterPreferencePhoneLengthRule(86400),
            DefaultRosterPreferenceCodePrefixRule("ACM"),
            DefaultRosterPreferenceRequiredTitleRule("default roster preference needs this field"),
        ),
    )
}
