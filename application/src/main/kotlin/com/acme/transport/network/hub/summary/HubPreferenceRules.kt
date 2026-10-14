package com.acme.transport.network.hub.summary

import com.acme.transport.kit.text.normalize

/** A rule a hub preference must pass. Check returns its problems, or none. */
interface HubPreferenceRule {
    fun check(item: HubPreference): MutableList<String>
}

data class HubPreferenceCodePrefixRule(var prefix: String) : HubPreferenceRule {
    override fun check(item: HubPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class HubPreferenceNameLengthRule(var maxLength: Int) : HubPreferenceRule {
    override fun check(item: HubPreference): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubPreferenceAllowedCategoryRule(var allowed: MutableList<String>) : HubPreferenceRule {
    override fun check(item: HubPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is missing")
        }
        return mutableListOf<String>()
    }
}

data class HubPreferenceRequiredCategoryRule(var message: String) : HubPreferenceRule {
    override fun check(item: HubPreference): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub preference. */
data class HubPreferenceValidator(var rules: MutableList<HubPreferenceRule>) {
    fun validate(item: HubPreference): MutableList<String> {
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

    fun requireValid(item: HubPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultHubPreferenceValidator(): HubPreferenceValidator {
    return HubPreferenceValidator(
        mutableListOf<HubPreferenceRule>(
            HubPreferenceCodePrefixRule("Q"),
            HubPreferenceNameLengthRule(86400),
            HubPreferenceAllowedCategoryRule(mutableListOf<String>("seasonal", "seasonal", "global")),
            HubPreferenceRequiredCategoryRule("hub preference needs this field"),
        ),
    )
}
