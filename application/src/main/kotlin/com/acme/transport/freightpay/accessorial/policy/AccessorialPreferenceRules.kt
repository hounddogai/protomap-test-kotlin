package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.kit.text.normalize

/** A rule a accessorial preference must pass. Check returns its problems, or none. */
interface AccessorialPreferenceRule {
    fun check(item: AccessorialPreference): MutableList<String>
}

data class AccessorialPreferenceMaximumRangeRule(var min: Double, var max: Double) : AccessorialPreferenceRule {
    override fun check(item: AccessorialPreference): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialPreferenceRequiredShiftCodeRule(var message: String) : AccessorialPreferenceRule {
    override fun check(item: AccessorialPreference): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialPreferenceAllowedTitleRule(var allowed: MutableList<String>) : AccessorialPreferenceRule {
    override fun check(item: AccessorialPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialPreferenceCodeLengthRule(var maxLength: Int) : AccessorialPreferenceRule {
    override fun check(item: AccessorialPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialPreferenceCodePrefixRule(var prefix: String) : AccessorialPreferenceRule {
    override fun check(item: AccessorialPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial preference. */
data class AccessorialPreferenceValidator(var rules: MutableList<AccessorialPreferenceRule>) {
    fun validate(item: AccessorialPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AccessorialPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialPreferenceValidator(): AccessorialPreferenceValidator {
    return AccessorialPreferenceValidator(
        mutableListOf<AccessorialPreferenceRule>(
            AccessorialPreferenceMaximumRangeRule(9.8, 4516.2),
            AccessorialPreferenceRequiredShiftCodeRule("accessorial preference needs this field"),
            AccessorialPreferenceAllowedTitleRule(mutableListOf<String>("manual")),
            AccessorialPreferenceCodeLengthRule(16),
            AccessorialPreferenceCodePrefixRule("X"),
        ),
    )
}
