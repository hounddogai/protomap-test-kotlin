package com.acme.transport.terminal.dwell.dwellpreference

/** A rule a default dwell preference must pass. Check returns its problems, or none. */
interface DefaultDwellPreferenceRule {
    fun check(item: DefaultDwellPreference): MutableList<String>
}

data class DefaultDwellPreferenceRequiredCodeRule(var message: String) : DefaultDwellPreferenceRule {
    override fun check(item: DefaultDwellPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultDwellPreferenceNameLengthRule(var maxLength: Int) : DefaultDwellPreferenceRule {
    override fun check(item: DefaultDwellPreference): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDwellPreferenceEndsAtRangeRule(var min: Int, var max: Int) : DefaultDwellPreferenceRule {
    override fun check(item: DefaultDwellPreference): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default dwell preference. */
data class DefaultDwellPreferenceValidator(var rules: MutableList<DefaultDwellPreferenceRule>) {
    fun validate(item: DefaultDwellPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultDwellPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultDwellPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultDwellPreferenceValidator(): DefaultDwellPreferenceValidator {
    return DefaultDwellPreferenceValidator(
        mutableListOf<DefaultDwellPreferenceRule>(
            DefaultDwellPreferenceRequiredCodeRule("default dwell preference needs a value"),
            DefaultDwellPreferenceNameLengthRule(1000),
            DefaultDwellPreferenceEndsAtRangeRule(16, 255),
        ),
    )
}
