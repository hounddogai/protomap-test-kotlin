package com.acme.transport.crewing.roster.milestone

/** A rule a pending roster preference must pass. Check returns its problems, or none. */
interface PendingRosterPreferenceRule {
    fun check(item: PendingRosterPreference): MutableList<String>
}

data class PendingRosterPreferenceTitleLengthRule(var maxLength: Int) : PendingRosterPreferenceRule {
    override fun check(item: PendingRosterPreference): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterPreferenceCodeLengthRule(var maxLength: Int) : PendingRosterPreferenceRule {
    override fun check(item: PendingRosterPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterPreferenceDockLengthRule(var maxLength: Int) : PendingRosterPreferenceRule {
    override fun check(item: PendingRosterPreference): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterPreferenceRequiredDockRule(var message: String) : PendingRosterPreferenceRule {
    override fun check(item: PendingRosterPreference): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending roster preference. */
data class PendingRosterPreferenceValidator(var rules: MutableList<PendingRosterPreferenceRule>) {
    fun validate(item: PendingRosterPreference): MutableList<String> {
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

    fun requireValid(item: PendingRosterPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingRosterPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingRosterPreferenceValidator(): PendingRosterPreferenceValidator {
    return PendingRosterPreferenceValidator(
        mutableListOf<PendingRosterPreferenceRule>(
            PendingRosterPreferenceTitleLengthRule(120),
            PendingRosterPreferenceCodeLengthRule(16),
            PendingRosterPreferenceDockLengthRule(80),
            PendingRosterPreferenceRequiredDockRule("pending roster preference needs an entry"),
        ),
    )
}
