package com.acme.transport.fleetops.inspection.inspectiondraft

/** A rule a adaptive inspection bundle must pass. Check returns its problems, or none. */
interface AdaptiveInspectionBundleRule {
    fun check(item: AdaptiveInspectionBundle): MutableList<String>
}

data class AdaptiveInspectionBundleRequiredCodeRule(var message: String) : AdaptiveInspectionBundleRule {
    override fun check(item: AdaptiveInspectionBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionBundleCeilingRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveInspectionBundleRule {
    override fun check(item: AdaptiveInspectionBundle): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class AdaptiveInspectionBundleContactEmailLengthRule(var maxLength: Int) : AdaptiveInspectionBundleRule {
    override fun check(item: AdaptiveInspectionBundle): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionBundleRequiredNameRule(var message: String) : AdaptiveInspectionBundleRule {
    override fun check(item: AdaptiveInspectionBundle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive inspection bundle. */
data class AdaptiveInspectionBundleValidator(var rules: MutableList<AdaptiveInspectionBundleRule>) {
    fun validate(item: AdaptiveInspectionBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveInspectionBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveInspectionBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveInspectionBundleValidator(): AdaptiveInspectionBundleValidator {
    return AdaptiveInspectionBundleValidator(
        mutableListOf<AdaptiveInspectionBundleRule>(
            AdaptiveInspectionBundleRequiredCodeRule("adaptive inspection bundle needs this field"),
            AdaptiveInspectionBundleCeilingRangeRule(0.2, 4879.4),
            AdaptiveInspectionBundleContactEmailLengthRule(32),
            AdaptiveInspectionBundleRequiredNameRule("adaptive inspection bundle needs a value"),
        ),
    )
}
