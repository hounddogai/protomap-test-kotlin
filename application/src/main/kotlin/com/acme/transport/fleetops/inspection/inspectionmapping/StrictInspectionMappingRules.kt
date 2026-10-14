package com.acme.transport.fleetops.inspection.inspectionmapping

/** A rule a strict inspection mapping must pass. Check returns its problems, or none. */
interface StrictInspectionMappingRule {
    fun check(item: StrictInspectionMapping): MutableList<String>
}

data class StrictInspectionMappingTitleLengthRule(var maxLength: Int) : StrictInspectionMappingRule {
    override fun check(item: StrictInspectionMapping): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictInspectionMappingRequiredCodeRule(var message: String) : StrictInspectionMappingRule {
    override fun check(item: StrictInspectionMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictInspectionMappingDiscountRangeRule(
    var min: Double,
    var max: Double,
) : StrictInspectionMappingRule {
    override fun check(item: StrictInspectionMapping): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict inspection mapping. */
data class StrictInspectionMappingValidator(var rules: MutableList<StrictInspectionMappingRule>) {
    fun validate(item: StrictInspectionMapping): MutableList<String> {
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

    fun requireValid(item: StrictInspectionMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictInspectionMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictInspectionMappingValidator(): StrictInspectionMappingValidator {
    return StrictInspectionMappingValidator(
        mutableListOf<StrictInspectionMappingRule>(
            StrictInspectionMappingTitleLengthRule(1000),
            StrictInspectionMappingRequiredCodeRule("strict inspection mapping needs an entry"),
            StrictInspectionMappingDiscountRangeRule(3.7, 3945.9),
        ),
    )
}
