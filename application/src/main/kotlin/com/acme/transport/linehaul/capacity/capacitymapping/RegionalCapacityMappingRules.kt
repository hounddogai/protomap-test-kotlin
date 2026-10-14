package com.acme.transport.linehaul.capacity.capacitymapping

import com.acme.transport.kit.text.normalize

/** A rule a regional capacity mapping must pass. Check returns its problems, or none. */
interface RegionalCapacityMappingRule {
    fun check(item: RegionalCapacityMapping): MutableList<String>
}

data class RegionalCapacityMappingThresholdRangeRule(
    var min: Double,
    var max: Double,
) : RegionalCapacityMappingRule {
    override fun check(item: RegionalCapacityMapping): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityMappingAllowedSourceRule(
    var allowed: MutableList<String>,
) : RegionalCapacityMappingRule {
    override fun check(item: RegionalCapacityMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityMappingRequiredCodeRule(var message: String) : RegionalCapacityMappingRule {
    override fun check(item: RegionalCapacityMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional capacity mapping. */
data class RegionalCapacityMappingValidator(var rules: MutableList<RegionalCapacityMappingRule>) {
    fun validate(item: RegionalCapacityMapping): MutableList<String> {
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

    fun requireValid(item: RegionalCapacityMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCapacityMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCapacityMappingValidator(): RegionalCapacityMappingValidator {
    return RegionalCapacityMappingValidator(
        mutableListOf<RegionalCapacityMappingRule>(
            RegionalCapacityMappingThresholdRangeRule(3.1, 860.8),
            RegionalCapacityMappingAllowedSourceRule(mutableListOf<String>()),
            RegionalCapacityMappingRequiredCodeRule("regional capacity mapping needs a value"),
        ),
    )
}
