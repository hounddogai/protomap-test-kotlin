package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.text.normalize

/** A rule a coaching mapping must pass. Check returns its problems, or none. */
interface CoachingMappingRule {
    fun check(item: CoachingMapping): MutableList<String>
}

data class CoachingMappingCodeLengthRule(var maxLength: Int) : CoachingMappingRule {
    override fun check(item: CoachingMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingMappingCodePrefixRule(var prefix: String) : CoachingMappingRule {
    override fun check(item: CoachingMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CoachingMappingRequiredDepotRule(var message: String) : CoachingMappingRule {
    override fun check(item: CoachingMapping): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingMappingQuantityRangeRule(var min: Int, var max: Int) : CoachingMappingRule {
    override fun check(item: CoachingMapping): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class CoachingMappingDepotLengthRule(var maxLength: Int) : CoachingMappingRule {
    override fun check(item: CoachingMapping): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingMappingAllowedDepotRule(var allowed: MutableList<String>) : CoachingMappingRule {
    override fun check(item: CoachingMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching mapping. */
data class CoachingMappingValidator(var rules: MutableList<CoachingMappingRule>) {
    fun validate(item: CoachingMapping): MutableList<String> {
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

    fun requireValid(item: CoachingMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingMappingValidator(): CoachingMappingValidator {
    return CoachingMappingValidator(
        mutableListOf<CoachingMappingRule>(
            CoachingMappingCodeLengthRule(32),
            CoachingMappingCodePrefixRule("ACM"),
            CoachingMappingRequiredDepotRule("coaching mapping needs a value"),
            CoachingMappingQuantityRangeRule(16, 64),
            CoachingMappingDepotLengthRule(120),
            CoachingMappingAllowedDepotRule(mutableListOf<String>("weighted")),
        ),
    )
}
