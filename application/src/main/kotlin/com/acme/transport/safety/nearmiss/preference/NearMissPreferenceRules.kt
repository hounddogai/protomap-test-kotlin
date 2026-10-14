package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.kit.text.normalize

/** A rule a near miss preference must pass. Check returns its problems, or none. */
interface NearMissPreferenceRule {
    fun check(item: NearMissPreference): MutableList<String>
}

data class NearMissPreferenceDepotLengthRule(var maxLength: Int) : NearMissPreferenceRule {
    override fun check(item: NearMissPreference): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissPreferenceRequiredLabelRule(var message: String) : NearMissPreferenceRule {
    override fun check(item: NearMissPreference): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissPreferenceRequiredCodeRule(var message: String) : NearMissPreferenceRule {
    override fun check(item: NearMissPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissPreferenceCodePrefixRule(var prefix: String) : NearMissPreferenceRule {
    override fun check(item: NearMissPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class NearMissPreferenceAllowedCodeRule(var allowed: MutableList<String>) : NearMissPreferenceRule {
    override fun check(item: NearMissPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class NearMissPreferenceCodeLengthRule(var maxLength: Int) : NearMissPreferenceRule {
    override fun check(item: NearMissPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss preference. */
data class NearMissPreferenceValidator(var rules: MutableList<NearMissPreferenceRule>) {
    fun validate(item: NearMissPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NearMissPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissPreferenceValidator(): NearMissPreferenceValidator {
    return NearMissPreferenceValidator(
        mutableListOf<NearMissPreferenceRule>(
            NearMissPreferenceDepotLengthRule(3600),
            NearMissPreferenceRequiredLabelRule("near miss preference needs a value"),
            NearMissPreferenceRequiredCodeRule("near miss preference needs a value"),
            NearMissPreferenceCodePrefixRule("REF"),
            NearMissPreferenceAllowedCodeRule(mutableListOf<String>("lenient", "lenient")),
            NearMissPreferenceCodeLengthRule(86400),
        ),
    )
}
