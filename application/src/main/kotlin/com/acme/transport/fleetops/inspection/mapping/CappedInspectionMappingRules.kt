package com.acme.transport.fleetops.inspection.mapping

import com.acme.transport.kit.text.normalize

/** A rule a capped inspection mapping must pass. Check returns its problems, or none. */
interface CappedInspectionMappingRule {
    fun check(item: CappedInspectionMapping): MutableList<String>
}

data class CappedInspectionMappingCodePrefixRule(var prefix: String) : CappedInspectionMappingRule {
    override fun check(item: CappedInspectionMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionMappingAllowedCodeRule(
    var allowed: MutableList<String>,
) : CappedInspectionMappingRule {
    override fun check(item: CappedInspectionMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionMappingDescriptionLengthRule(var maxLength: Int) : CappedInspectionMappingRule {
    override fun check(item: CappedInspectionMapping): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped inspection mapping. */
data class CappedInspectionMappingValidator(var rules: MutableList<CappedInspectionMappingRule>) {
    fun validate(item: CappedInspectionMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedInspectionMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedInspectionMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedInspectionMappingValidator(): CappedInspectionMappingValidator {
    return CappedInspectionMappingValidator(
        mutableListOf<CappedInspectionMappingRule>(
            CappedInspectionMappingCodePrefixRule("OPS"),
            CappedInspectionMappingAllowedCodeRule(mutableListOf<String>()),
            CappedInspectionMappingDescriptionLengthRule(80),
        ),
    )
}
