package com.acme.transport.fleetops.inspection.inspectionsummary

/** A rule a global inspection template must pass. Check returns its problems, or none. */
interface GlobalInspectionTemplateRule {
    fun check(item: GlobalInspectionTemplate): MutableList<String>
}

data class GlobalInspectionTemplateFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : GlobalInspectionTemplateRule {
    override fun check(item: GlobalInspectionTemplate): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class GlobalInspectionTemplateRequiredCountryRule(var message: String) : GlobalInspectionTemplateRule {
    override fun check(item: GlobalInspectionTemplate): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionTemplateReviewerLengthRule(var maxLength: Int) : GlobalInspectionTemplateRule {
    override fun check(item: GlobalInspectionTemplate): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global inspection template. */
data class GlobalInspectionTemplateValidator(var rules: MutableList<GlobalInspectionTemplateRule>) {
    fun validate(item: GlobalInspectionTemplate): MutableList<String> {
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

    fun requireValid(item: GlobalInspectionTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalInspectionTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalInspectionTemplateValidator(): GlobalInspectionTemplateValidator {
    return GlobalInspectionTemplateValidator(
        mutableListOf<GlobalInspectionTemplateRule>(
            GlobalInspectionTemplateFloorValueRangeRule(9.1, 760.9),
            GlobalInspectionTemplateRequiredCountryRule("global inspection template needs an entry"),
            GlobalInspectionTemplateReviewerLengthRule(120),
        ),
    )
}
