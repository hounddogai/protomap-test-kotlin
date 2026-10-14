package com.acme.transport.network.hub.mapping

import com.acme.transport.kit.text.normalize

/** A rule a hub mapping must pass. Check returns its problems, or none. */
interface HubMappingRule {
    fun check(item: HubMapping): MutableList<String>
}

data class HubMappingAllowedTrailerIdRule(var allowed: MutableList<String>) : HubMappingRule {
    override fun check(item: HubMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class HubMappingTrailerIdLengthRule(var maxLength: Int) : HubMappingRule {
    override fun check(item: HubMapping): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubMappingCodeLengthRule(var maxLength: Int) : HubMappingRule {
    override fun check(item: HubMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubMappingRatioRangeRule(var min: Double, var max: Double) : HubMappingRule {
    override fun check(item: HubMapping): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HubMappingRequiredCodeRule(var message: String) : HubMappingRule {
    override fun check(item: HubMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub mapping. */
data class HubMappingValidator(var rules: MutableList<HubMappingRule>) {
    fun validate(item: HubMapping): MutableList<String> {
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

    fun requireValid(item: HubMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultHubMappingValidator(): HubMappingValidator {
    return HubMappingValidator(
        mutableListOf<HubMappingRule>(
            HubMappingAllowedTrailerIdRule(mutableListOf<String>()),
            HubMappingTrailerIdLengthRule(255),
            HubMappingCodeLengthRule(120),
            HubMappingRatioRangeRule(6.0, 4779.9),
            HubMappingRequiredCodeRule("hub mapping needs this field"),
        ),
    )
}
