package com.acme.transport.terminal.dockdoor.doorestimate

import com.acme.transport.kit.text.normalize

/** A rule a default dock door preference must pass. Check returns its problems, or none. */
interface DefaultDockDoorPreferenceRule {
    fun check(item: DefaultDockDoorPreference): MutableList<String>
}

data class DefaultDockDoorPreferenceRequiredNameRule(var message: String) : DefaultDockDoorPreferenceRule {
    override fun check(item: DefaultDockDoorPreference): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultDockDoorPreferenceAllowedTimezoneRule(
    var allowed: MutableList<String>,
) : DefaultDockDoorPreferenceRule {
    override fun check(item: DefaultDockDoorPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is missing")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDockDoorPreferenceTimezoneLengthRule(var maxLength: Int) : DefaultDockDoorPreferenceRule {
    override fun check(item: DefaultDockDoorPreference): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default dock door preference. */
data class DefaultDockDoorPreferenceValidator(var rules: MutableList<DefaultDockDoorPreferenceRule>) {
    fun validate(item: DefaultDockDoorPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultDockDoorPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultDockDoorPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultDockDoorPreferenceValidator(): DefaultDockDoorPreferenceValidator {
    return DefaultDockDoorPreferenceValidator(
        mutableListOf<DefaultDockDoorPreferenceRule>(
            DefaultDockDoorPreferenceRequiredNameRule("default dock door preference needs this field"),
            DefaultDockDoorPreferenceAllowedTimezoneRule(mutableListOf<String>("manual")),
            DefaultDockDoorPreferenceTimezoneLengthRule(80),
        ),
    )
}
