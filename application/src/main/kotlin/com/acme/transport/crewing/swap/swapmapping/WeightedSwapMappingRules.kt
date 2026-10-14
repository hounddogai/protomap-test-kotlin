package com.acme.transport.crewing.swap.swapmapping

import com.acme.transport.kit.text.normalize

/** A rule a weighted swap mapping must pass. Check returns its problems, or none. */
interface WeightedSwapMappingRule {
    fun check(item: WeightedSwapMapping): MutableList<String>
}

data class WeightedSwapMappingCategoryLengthRule(var maxLength: Int) : WeightedSwapMappingRule {
    override fun check(item: WeightedSwapMapping): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedSwapMappingRequiredVehicleIdRule(var message: String) : WeightedSwapMappingRule {
    override fun check(item: WeightedSwapMapping): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedSwapMappingRequiredCodeRule(var message: String) : WeightedSwapMappingRule {
    override fun check(item: WeightedSwapMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedSwapMappingAllowedCodeRule(var allowed: MutableList<String>) : WeightedSwapMappingRule {
    override fun check(item: WeightedSwapMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class WeightedSwapMappingLabelLengthRule(var maxLength: Int) : WeightedSwapMappingRule {
    override fun check(item: WeightedSwapMapping): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted swap mapping. */
data class WeightedSwapMappingValidator(var rules: MutableList<WeightedSwapMappingRule>) {
    fun validate(item: WeightedSwapMapping): MutableList<String> {
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

    fun requireValid(item: WeightedSwapMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedSwapMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedSwapMappingValidator(): WeightedSwapMappingValidator {
    return WeightedSwapMappingValidator(
        mutableListOf<WeightedSwapMappingRule>(
            WeightedSwapMappingCategoryLengthRule(32),
            WeightedSwapMappingRequiredVehicleIdRule("weighted swap mapping needs an entry"),
            WeightedSwapMappingRequiredCodeRule("weighted swap mapping needs this field"),
            WeightedSwapMappingAllowedCodeRule(mutableListOf<String>("default", "rolling", "adaptive")),
            WeightedSwapMappingLabelLengthRule(80),
        ),
    )
}
