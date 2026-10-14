package com.acme.transport.freightpay.accessorial.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a accessorial mapping must pass. Check returns its problems, or none. */
interface AccessorialMappingRule {
    fun check(item: AccessorialMapping): MutableList<String>
}

data class AccessorialMappingCountryLengthRule(var maxLength: Int) : AccessorialMappingRule {
    override fun check(item: AccessorialMapping): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialMappingBatchSizeRangeRule(var min: Int, var max: Int) : AccessorialMappingRule {
    override fun check(item: AccessorialMapping): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class AccessorialMappingRequiredCountryRule(var message: String) : AccessorialMappingRule {
    override fun check(item: AccessorialMapping): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialMappingAllowedCountryRule(var allowed: MutableList<String>) : AccessorialMappingRule {
    override fun check(item: AccessorialMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialMappingCodeLengthRule(var maxLength: Int) : AccessorialMappingRule {
    override fun check(item: AccessorialMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialMappingRequiredLabelRule(var message: String) : AccessorialMappingRule {
    override fun check(item: AccessorialMapping): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial mapping. */
data class AccessorialMappingValidator(var rules: MutableList<AccessorialMappingRule>) {
    fun validate(item: AccessorialMapping): MutableList<String> {
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

    fun requireValid(item: AccessorialMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialMappingValidator(): AccessorialMappingValidator {
    return AccessorialMappingValidator(
        mutableListOf<AccessorialMappingRule>(
            AccessorialMappingCountryLengthRule(32),
            AccessorialMappingBatchSizeRangeRule(16, 32),
            AccessorialMappingRequiredCountryRule("accessorial mapping needs an entry"),
            AccessorialMappingAllowedCountryRule(mutableListOf<String>("global")),
            AccessorialMappingCodeLengthRule(32),
            AccessorialMappingRequiredLabelRule("accessorial mapping needs an entry"),
        ),
    )
}
