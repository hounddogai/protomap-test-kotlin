package com.acme.transport.safety.coaching.mapping

import com.acme.transport.kit.text.normalize

/** A rule a primary coaching mapping must pass. Check returns its problems, or none. */
interface PrimaryCoachingMappingRule {
    fun check(item: PrimaryCoachingMapping): MutableList<String>
}

data class PrimaryCoachingMappingCodePrefixRule(var prefix: String) : PrimaryCoachingMappingRule {
    override fun check(item: PrimaryCoachingMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCoachingMappingBudgetRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryCoachingMappingRule {
    override fun check(item: PrimaryCoachingMapping): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCoachingMappingAllowedCodeRule(
    var allowed: MutableList<String>,
) : PrimaryCoachingMappingRule {
    override fun check(item: PrimaryCoachingMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCoachingMappingCodeLengthRule(var maxLength: Int) : PrimaryCoachingMappingRule {
    override fun check(item: PrimaryCoachingMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary coaching mapping. */
data class PrimaryCoachingMappingValidator(var rules: MutableList<PrimaryCoachingMappingRule>) {
    fun validate(item: PrimaryCoachingMapping): MutableList<String> {
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

    fun requireValid(item: PrimaryCoachingMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCoachingMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCoachingMappingValidator(): PrimaryCoachingMappingValidator {
    return PrimaryCoachingMappingValidator(
        mutableListOf<PrimaryCoachingMappingRule>(
            PrimaryCoachingMappingCodePrefixRule("X"),
            PrimaryCoachingMappingBudgetRangeRule(0.6, 2031.1),
            PrimaryCoachingMappingAllowedCodeRule(mutableListOf<String>()),
            PrimaryCoachingMappingCodeLengthRule(80),
        ),
    )
}
