package com.acme.transport.emissions.carbonreport.allocation

/** A rule a fallback carbon report preference must pass. Check returns its problems, or none. */
interface FallbackCarbonReportPreferenceRule {
    fun check(item: FallbackCarbonReportPreference): MutableList<String>
}

data class FallbackCarbonReportPreferenceRevisionRangeRule(
    var min: Int,
    var max: Int,
) : FallbackCarbonReportPreferenceRule {
    override fun check(item: FallbackCarbonReportPreference): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCarbonReportPreferenceContactEmailLengthRule(
    var maxLength: Int,
) : FallbackCarbonReportPreferenceRule {
    override fun check(item: FallbackCarbonReportPreference): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCarbonReportPreferenceRequiredCodeRule(
    var message: String,
) : FallbackCarbonReportPreferenceRule {
    override fun check(item: FallbackCarbonReportPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback carbon report preference. */
data class FallbackCarbonReportPreferenceValidator(var rules: MutableList<FallbackCarbonReportPreferenceRule>) {
    fun validate(item: FallbackCarbonReportPreference): MutableList<String> {
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

    fun requireValid(item: FallbackCarbonReportPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackCarbonReportPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackCarbonReportPreferenceValidator(): FallbackCarbonReportPreferenceValidator {
    return FallbackCarbonReportPreferenceValidator(
        mutableListOf<FallbackCarbonReportPreferenceRule>(
            FallbackCarbonReportPreferenceRevisionRangeRule(64, 120),
            FallbackCarbonReportPreferenceContactEmailLengthRule(64),
            FallbackCarbonReportPreferenceRequiredCodeRule("fallback carbon report preference needs this field"),
        ),
    )
}
