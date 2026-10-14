package com.acme.transport.customs.inspection.inspectionentry

import com.acme.transport.kit.text.normalize

/** A rule a lenient inspection mapping must pass. Check returns its problems, or none. */
interface LenientInspectionMappingRule {
    fun check(item: LenientInspectionMapping): MutableList<String>
}

data class LenientInspectionMappingAllowedTitleRule(
    var allowed: MutableList<String>,
) : LenientInspectionMappingRule {
    override fun check(item: LenientInspectionMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is missing")
        }
        return mutableListOf<String>()
    }
}

data class LenientInspectionMappingTitleLengthRule(var maxLength: Int) : LenientInspectionMappingRule {
    override fun check(item: LenientInspectionMapping): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientInspectionMappingFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : LenientInspectionMappingRule {
    override fun check(item: LenientInspectionMapping): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientInspectionMappingCodeLengthRule(var maxLength: Int) : LenientInspectionMappingRule {
    override fun check(item: LenientInspectionMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient inspection mapping. */
data class LenientInspectionMappingValidator(var rules: MutableList<LenientInspectionMappingRule>) {
    fun validate(item: LenientInspectionMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientInspectionMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientInspectionMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientInspectionMappingValidator(): LenientInspectionMappingValidator {
    return LenientInspectionMappingValidator(
        mutableListOf<LenientInspectionMappingRule>(
            LenientInspectionMappingAllowedTitleRule(mutableListOf<String>("primary", "tiered", "strict")),
            LenientInspectionMappingTitleLengthRule(1000),
            LenientInspectionMappingFuelLitersRangeRule(5.9, 4899.6),
            LenientInspectionMappingCodeLengthRule(80),
        ),
    )
}
