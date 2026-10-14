package com.acme.transport.crewing.availability.manifest

import com.acme.transport.kit.text.normalize

/** A rule a adaptive availability mapping must pass. Check returns its problems, or none. */
interface AdaptiveAvailabilityMappingRule {
    fun check(item: AdaptiveAvailabilityMapping): MutableList<String>
}

data class AdaptiveAvailabilityMappingRequiredVehicleIdRule(
    var message: String,
) : AdaptiveAvailabilityMappingRule {
    override fun check(item: AdaptiveAvailabilityMapping): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAvailabilityMappingRequiredCodeRule(var message: String) : AdaptiveAvailabilityMappingRule {
    override fun check(item: AdaptiveAvailabilityMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAvailabilityMappingTitleLengthRule(var maxLength: Int) : AdaptiveAvailabilityMappingRule {
    override fun check(item: AdaptiveAvailabilityMapping): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAvailabilityMappingAllowedTitleRule(
    var allowed: MutableList<String>,
) : AdaptiveAvailabilityMappingRule {
    override fun check(item: AdaptiveAvailabilityMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is expired")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAvailabilityMappingCodePrefixRule(var prefix: String) : AdaptiveAvailabilityMappingRule {
    override fun check(item: AdaptiveAvailabilityMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAvailabilityMappingCodeLengthRule(var maxLength: Int) : AdaptiveAvailabilityMappingRule {
    override fun check(item: AdaptiveAvailabilityMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive availability mapping. */
data class AdaptiveAvailabilityMappingValidator(var rules: MutableList<AdaptiveAvailabilityMappingRule>) {
    fun validate(item: AdaptiveAvailabilityMapping): MutableList<String> {
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

    fun requireValid(item: AdaptiveAvailabilityMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveAvailabilityMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveAvailabilityMappingValidator(): AdaptiveAvailabilityMappingValidator {
    return AdaptiveAvailabilityMappingValidator(
        mutableListOf<AdaptiveAvailabilityMappingRule>(
            AdaptiveAvailabilityMappingRequiredVehicleIdRule("adaptive availability mapping needs a value"),
            AdaptiveAvailabilityMappingRequiredCodeRule("adaptive availability mapping needs an entry"),
            AdaptiveAvailabilityMappingTitleLengthRule(120),
            AdaptiveAvailabilityMappingAllowedTitleRule(mutableListOf<String>()),
            AdaptiveAvailabilityMappingCodePrefixRule("OPS"),
            AdaptiveAvailabilityMappingCodeLengthRule(1000),
        ),
    )
}
