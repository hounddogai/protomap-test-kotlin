package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a regional idling mapping must pass. Check returns its problems, or none. */
interface RegionalIdlingMappingRule {
    fun check(item: RegionalIdlingMapping): MutableList<String>
}

data class RegionalIdlingMappingRequiredSegmentRule(var message: String) : RegionalIdlingMappingRule {
    override fun check(item: RegionalIdlingMapping): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalIdlingMappingAllowedCodeRule(var allowed: MutableList<String>) : RegionalIdlingMappingRule {
    override fun check(item: RegionalIdlingMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIdlingMappingLaneLengthRule(var maxLength: Int) : RegionalIdlingMappingRule {
    override fun check(item: RegionalIdlingMapping): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIdlingMappingSegmentLengthRule(var maxLength: Int) : RegionalIdlingMappingRule {
    override fun check(item: RegionalIdlingMapping): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional idling mapping. */
data class RegionalIdlingMappingValidator(var rules: MutableList<RegionalIdlingMappingRule>) {
    fun validate(item: RegionalIdlingMapping): MutableList<String> {
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

    fun requireValid(item: RegionalIdlingMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalIdlingMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalIdlingMappingValidator(): RegionalIdlingMappingValidator {
    return RegionalIdlingMappingValidator(
        mutableListOf<RegionalIdlingMappingRule>(
            RegionalIdlingMappingRequiredSegmentRule("regional idling mapping needs an entry"),
            RegionalIdlingMappingAllowedCodeRule(mutableListOf<String>("global", "primary")),
            RegionalIdlingMappingLaneLengthRule(3600),
            RegionalIdlingMappingSegmentLengthRule(255),
        ),
    )
}
