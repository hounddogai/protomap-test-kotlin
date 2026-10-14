package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a manual backhaul override must pass. Check returns its problems, or none. */
interface ManualBackhaulOverrideRule {
    fun check(item: ManualBackhaulOverride): MutableList<String>
}

data class ManualBackhaulOverrideRequiredTitleRule(var message: String) : ManualBackhaulOverrideRule {
    override fun check(item: ManualBackhaulOverride): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualBackhaulOverrideCodePrefixRule(var prefix: String) : ManualBackhaulOverrideRule {
    override fun check(item: ManualBackhaulOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualBackhaulOverrideRequiredSummaryRule(var message: String) : ManualBackhaulOverrideRule {
    override fun check(item: ManualBackhaulOverride): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualBackhaulOverrideAllowedTimezoneRule(
    var allowed: MutableList<String>,
) : ManualBackhaulOverrideRule {
    override fun check(item: ManualBackhaulOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual backhaul override. */
data class ManualBackhaulOverrideValidator(var rules: MutableList<ManualBackhaulOverrideRule>) {
    fun validate(item: ManualBackhaulOverride): MutableList<String> {
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

    fun requireValid(item: ManualBackhaulOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualBackhaulOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultManualBackhaulOverrideValidator(): ManualBackhaulOverrideValidator {
    return ManualBackhaulOverrideValidator(
        mutableListOf<ManualBackhaulOverrideRule>(
            ManualBackhaulOverrideRequiredTitleRule("manual backhaul override needs this field"),
            ManualBackhaulOverrideCodePrefixRule("X"),
            ManualBackhaulOverrideRequiredSummaryRule("manual backhaul override needs a value"),
            ManualBackhaulOverrideAllowedTimezoneRule(mutableListOf<String>("default")),
        ),
    )
}
