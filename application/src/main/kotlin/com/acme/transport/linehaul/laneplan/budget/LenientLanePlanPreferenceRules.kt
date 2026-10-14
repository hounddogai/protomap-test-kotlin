package com.acme.transport.linehaul.laneplan.budget

/** A rule a lenient lane plan preference must pass. Check returns its problems, or none. */
interface LenientLanePlanPreferenceRule {
    fun check(item: LenientLanePlanPreference): MutableList<String>
}

data class LenientLanePlanPreferenceRequiredCodeRule(var message: String) : LenientLanePlanPreferenceRule {
    override fun check(item: LenientLanePlanPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientLanePlanPreferenceRequiredTrailerIdRule(var message: String) : LenientLanePlanPreferenceRule {
    override fun check(item: LenientLanePlanPreference): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientLanePlanPreferenceTrailerIdLengthRule(var maxLength: Int) : LenientLanePlanPreferenceRule {
    override fun check(item: LenientLanePlanPreference): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientLanePlanPreferenceCodePrefixRule(var prefix: String) : LenientLanePlanPreferenceRule {
    override fun check(item: LenientLanePlanPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientLanePlanPreferenceNameLengthRule(var maxLength: Int) : LenientLanePlanPreferenceRule {
    override fun check(item: LenientLanePlanPreference): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientLanePlanPreferenceCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : LenientLanePlanPreferenceRule {
    override fun check(item: LenientLanePlanPreference): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient lane plan preference. */
data class LenientLanePlanPreferenceValidator(var rules: MutableList<LenientLanePlanPreferenceRule>) {
    fun validate(item: LenientLanePlanPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientLanePlanPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientLanePlanPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientLanePlanPreferenceValidator(): LenientLanePlanPreferenceValidator {
    return LenientLanePlanPreferenceValidator(
        mutableListOf<LenientLanePlanPreferenceRule>(
            LenientLanePlanPreferenceRequiredCodeRule("lenient lane plan preference needs a value"),
            LenientLanePlanPreferenceRequiredTrailerIdRule("lenient lane plan preference needs an entry"),
            LenientLanePlanPreferenceTrailerIdLengthRule(1000),
            LenientLanePlanPreferenceCodePrefixRule("X"),
            LenientLanePlanPreferenceNameLengthRule(16),
            LenientLanePlanPreferenceCreatedAtRangeRule(1000, 3600),
        ),
    )
}
