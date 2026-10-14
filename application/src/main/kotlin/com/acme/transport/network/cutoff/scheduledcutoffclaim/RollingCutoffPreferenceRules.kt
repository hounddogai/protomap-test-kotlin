package com.acme.transport.network.cutoff.scheduledcutoffclaim

/** A rule a rolling cutoff preference must pass. Check returns its problems, or none. */
interface RollingCutoffPreferenceRule {
    fun check(item: RollingCutoffPreference): MutableList<String>
}

data class RollingCutoffPreferenceRequiredCodeRule(var message: String) : RollingCutoffPreferenceRule {
    override fun check(item: RollingCutoffPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffPreferenceNameLengthRule(var maxLength: Int) : RollingCutoffPreferenceRule {
    override fun check(item: RollingCutoffPreference): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffPreferenceShiftCodeLengthRule(var maxLength: Int) : RollingCutoffPreferenceRule {
    override fun check(item: RollingCutoffPreference): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffPreferenceRequiredNameRule(var message: String) : RollingCutoffPreferenceRule {
    override fun check(item: RollingCutoffPreference): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffPreferenceRequiredShiftCodeRule(var message: String) : RollingCutoffPreferenceRule {
    override fun check(item: RollingCutoffPreference): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffPreferenceCodePrefixRule(var prefix: String) : RollingCutoffPreferenceRule {
    override fun check(item: RollingCutoffPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling cutoff preference. */
data class RollingCutoffPreferenceValidator(var rules: MutableList<RollingCutoffPreferenceRule>) {
    fun validate(item: RollingCutoffPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingCutoffPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingCutoffPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingCutoffPreferenceValidator(): RollingCutoffPreferenceValidator {
    return RollingCutoffPreferenceValidator(
        mutableListOf<RollingCutoffPreferenceRule>(
            RollingCutoffPreferenceRequiredCodeRule("rolling cutoff preference needs an entry"),
            RollingCutoffPreferenceNameLengthRule(255),
            RollingCutoffPreferenceShiftCodeLengthRule(32),
            RollingCutoffPreferenceRequiredNameRule("rolling cutoff preference needs this field"),
            RollingCutoffPreferenceRequiredShiftCodeRule("rolling cutoff preference needs an entry"),
            RollingCutoffPreferenceCodePrefixRule("X"),
        ),
    )
}
