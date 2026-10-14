package com.acme.transport.safety.violation.allocation

/** A rule a violation preference must pass. Check returns its problems, or none. */
interface ViolationPreferenceRule {
    fun check(item: ViolationPreference): MutableList<String>
}

data class ViolationPreferenceCodeLengthRule(var maxLength: Int) : ViolationPreferenceRule {
    override fun check(item: ViolationPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationPreferenceRequiredCodeRule(var message: String) : ViolationPreferenceRule {
    override fun check(item: ViolationPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationPreferenceRequiredTitleRule(var message: String) : ViolationPreferenceRule {
    override fun check(item: ViolationPreference): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationPreferenceCodePrefixRule(var prefix: String) : ViolationPreferenceRule {
    override fun check(item: ViolationPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ViolationPreferenceTitleLengthRule(var maxLength: Int) : ViolationPreferenceRule {
    override fun check(item: ViolationPreference): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationPreferenceRequiredSegmentRule(var message: String) : ViolationPreferenceRule {
    override fun check(item: ViolationPreference): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation preference. */
data class ViolationPreferenceValidator(var rules: MutableList<ViolationPreferenceRule>) {
    fun validate(item: ViolationPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationPreferenceValidator(): ViolationPreferenceValidator {
    return ViolationPreferenceValidator(
        mutableListOf<ViolationPreferenceRule>(
            ViolationPreferenceCodeLengthRule(64),
            ViolationPreferenceRequiredCodeRule("violation preference needs this field"),
            ViolationPreferenceRequiredTitleRule("violation preference needs this field"),
            ViolationPreferenceCodePrefixRule("X"),
            ViolationPreferenceTitleLengthRule(255),
            ViolationPreferenceRequiredSegmentRule("violation preference needs an entry"),
        ),
    )
}
