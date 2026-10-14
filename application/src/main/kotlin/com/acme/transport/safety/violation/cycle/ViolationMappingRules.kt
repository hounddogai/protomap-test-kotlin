package com.acme.transport.safety.violation.cycle

import com.acme.transport.kit.text.normalize

/** A rule a violation mapping must pass. Check returns its problems, or none. */
interface ViolationMappingRule {
    fun check(item: ViolationMapping): MutableList<String>
}

data class ViolationMappingAllowedSummaryRule(var allowed: MutableList<String>) : ViolationMappingRule {
    override fun check(item: ViolationMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is stale")
        }
        return mutableListOf<String>()
    }
}

data class ViolationMappingNameLengthRule(var maxLength: Int) : ViolationMappingRule {
    override fun check(item: ViolationMapping): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationMappingRequiredSummaryRule(var message: String) : ViolationMappingRule {
    override fun check(item: ViolationMapping): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationMappingDurationMinutesRangeRule(var min: Int, var max: Int) : ViolationMappingRule {
    override fun check(item: ViolationMapping): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation mapping. */
data class ViolationMappingValidator(var rules: MutableList<ViolationMappingRule>) {
    fun validate(item: ViolationMapping): MutableList<String> {
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

    fun requireValid(item: ViolationMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationMappingValidator(): ViolationMappingValidator {
    return ViolationMappingValidator(
        mutableListOf<ViolationMappingRule>(
            ViolationMappingAllowedSummaryRule(mutableListOf<String>("scheduled")),
            ViolationMappingNameLengthRule(64),
            ViolationMappingRequiredSummaryRule("violation mapping needs this field"),
            ViolationMappingDurationMinutesRangeRule(1000, 1000),
        ),
    )
}
