package com.acme.transport.crewing.restbreak.mapping

import com.acme.transport.kit.text.normalize

/** A rule a rest break mapping must pass. Check returns its problems, or none. */
interface RestBreakMappingRule {
    fun check(item: RestBreakMapping): MutableList<String>
}

data class RestBreakMappingAllowedLabelRule(var allowed: MutableList<String>) : RestBreakMappingRule {
    override fun check(item: RestBreakMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakMappingSequenceRangeRule(var min: Int, var max: Int) : RestBreakMappingRule {
    override fun check(item: RestBreakMapping): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakMappingRequiredTimezoneRule(var message: String) : RestBreakMappingRule {
    override fun check(item: RestBreakMapping): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakMappingCodePrefixRule(var prefix: String) : RestBreakMappingRule {
    override fun check(item: RestBreakMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakMappingCodeLengthRule(var maxLength: Int) : RestBreakMappingRule {
    override fun check(item: RestBreakMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakMappingRequiredCodeRule(var message: String) : RestBreakMappingRule {
    override fun check(item: RestBreakMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break mapping. */
data class RestBreakMappingValidator(var rules: MutableList<RestBreakMappingRule>) {
    fun validate(item: RestBreakMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RestBreakMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakMappingValidator(): RestBreakMappingValidator {
    return RestBreakMappingValidator(
        mutableListOf<RestBreakMappingRule>(
            RestBreakMappingAllowedLabelRule(mutableListOf<String>("manual", "adaptive")),
            RestBreakMappingSequenceRangeRule(16, 64),
            RestBreakMappingRequiredTimezoneRule("rest break mapping needs this field"),
            RestBreakMappingCodePrefixRule("ACM"),
            RestBreakMappingCodeLengthRule(120),
            RestBreakMappingRequiredCodeRule("rest break mapping needs a value"),
        ),
    )
}
