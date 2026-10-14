package com.acme.transport.linehaul.tender.proposal

import com.acme.transport.kit.text.normalize

/** A rule a manual tender preference must pass. Check returns its problems, or none. */
interface ManualTenderPreferenceRule {
    fun check(item: ManualTenderPreference): MutableList<String>
}

data class ManualTenderPreferenceRequiredLabelRule(var message: String) : ManualTenderPreferenceRule {
    override fun check(item: ManualTenderPreference): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTenderPreferenceAllowedCodeRule(
    var allowed: MutableList<String>,
) : ManualTenderPreferenceRule {
    override fun check(item: ManualTenderPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class ManualTenderPreferenceCodeLengthRule(var maxLength: Int) : ManualTenderPreferenceRule {
    override fun check(item: ManualTenderPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTenderPreferenceRequiredDescriptionRule(var message: String) : ManualTenderPreferenceRule {
    override fun check(item: ManualTenderPreference): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTenderPreferenceCodePrefixRule(var prefix: String) : ManualTenderPreferenceRule {
    override fun check(item: ManualTenderPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualTenderPreferenceDescriptionLengthRule(var maxLength: Int) : ManualTenderPreferenceRule {
    override fun check(item: ManualTenderPreference): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual tender preference. */
data class ManualTenderPreferenceValidator(var rules: MutableList<ManualTenderPreferenceRule>) {
    fun validate(item: ManualTenderPreference): MutableList<String> {
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

    fun requireValid(item: ManualTenderPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTenderPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTenderPreferenceValidator(): ManualTenderPreferenceValidator {
    return ManualTenderPreferenceValidator(
        mutableListOf<ManualTenderPreferenceRule>(
            ManualTenderPreferenceRequiredLabelRule("manual tender preference needs this field"),
            ManualTenderPreferenceAllowedCodeRule(mutableListOf<String>("adaptive", "tiered")),
            ManualTenderPreferenceCodeLengthRule(64),
            ManualTenderPreferenceRequiredDescriptionRule("manual tender preference needs this field"),
            ManualTenderPreferenceCodePrefixRule("Q"),
            ManualTenderPreferenceDescriptionLengthRule(32),
        ),
    )
}
