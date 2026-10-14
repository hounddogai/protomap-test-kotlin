package com.acme.transport.linehaul.backhaul.preference

/** A rule a backhaul preference must pass. Check returns its problems, or none. */
interface BackhaulPreferenceRule {
    fun check(item: BackhaulPreference): MutableList<String>
}

data class BackhaulPreferenceRequiredNameRule(var message: String) : BackhaulPreferenceRule {
    override fun check(item: BackhaulPreference): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulPreferenceRequiredCodeRule(var message: String) : BackhaulPreferenceRule {
    override fun check(item: BackhaulPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulPreferenceCodePrefixRule(var prefix: String) : BackhaulPreferenceRule {
    override fun check(item: BackhaulPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul preference. */
data class BackhaulPreferenceValidator(var rules: MutableList<BackhaulPreferenceRule>) {
    fun validate(item: BackhaulPreference): MutableList<String> {
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

    fun requireValid(item: BackhaulPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulPreferenceValidator(): BackhaulPreferenceValidator {
    return BackhaulPreferenceValidator(
        mutableListOf<BackhaulPreferenceRule>(
            BackhaulPreferenceRequiredNameRule("backhaul preference needs this field"),
            BackhaulPreferenceRequiredCodeRule("backhaul preference needs an entry"),
            BackhaulPreferenceCodePrefixRule("OPS"),
        ),
    )
}
