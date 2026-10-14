package com.acme.transport.fleetops.utilization.utilizationreservation

import com.acme.transport.kit.text.normalize

/** A rule a global utilization mapping must pass. Check returns its problems, or none. */
interface GlobalUtilizationMappingRule {
    fun check(item: GlobalUtilizationMapping): MutableList<String>
}

data class GlobalUtilizationMappingNameLengthRule(var maxLength: Int) : GlobalUtilizationMappingRule {
    override fun check(item: GlobalUtilizationMapping): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalUtilizationMappingDiscountRangeRule(
    var min: Double,
    var max: Double,
) : GlobalUtilizationMappingRule {
    override fun check(item: GlobalUtilizationMapping): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalUtilizationMappingAllowedCodeRule(
    var allowed: MutableList<String>,
) : GlobalUtilizationMappingRule {
    override fun check(item: GlobalUtilizationMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class GlobalUtilizationMappingCodeLengthRule(var maxLength: Int) : GlobalUtilizationMappingRule {
    override fun check(item: GlobalUtilizationMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalUtilizationMappingRequiredCodeRule(var message: String) : GlobalUtilizationMappingRule {
    override fun check(item: GlobalUtilizationMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global utilization mapping. */
data class GlobalUtilizationMappingValidator(var rules: MutableList<GlobalUtilizationMappingRule>) {
    fun validate(item: GlobalUtilizationMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalUtilizationMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalUtilizationMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalUtilizationMappingValidator(): GlobalUtilizationMappingValidator {
    return GlobalUtilizationMappingValidator(
        mutableListOf<GlobalUtilizationMappingRule>(
            GlobalUtilizationMappingNameLengthRule(16),
            GlobalUtilizationMappingDiscountRangeRule(5.6, 4001.4),
            GlobalUtilizationMappingAllowedCodeRule(mutableListOf<String>()),
            GlobalUtilizationMappingCodeLengthRule(120),
            GlobalUtilizationMappingRequiredCodeRule("global utilization mapping needs a value"),
        ),
    )
}
