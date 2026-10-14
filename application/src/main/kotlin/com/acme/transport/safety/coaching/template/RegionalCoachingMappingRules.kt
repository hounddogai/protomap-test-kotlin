package com.acme.transport.safety.coaching.template

import com.acme.transport.kit.text.normalize

/** A rule a regional coaching mapping must pass. Check returns its problems, or none. */
interface RegionalCoachingMappingRule {
    fun check(item: RegionalCoachingMapping): MutableList<String>
}

data class RegionalCoachingMappingRequiredCarrierRule(var message: String) : RegionalCoachingMappingRule {
    override fun check(item: RegionalCoachingMapping): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCoachingMappingAllowedTimezoneRule(
    var allowed: MutableList<String>,
) : RegionalCoachingMappingRule {
    override fun check(item: RegionalCoachingMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is missing")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCoachingMappingTitleLengthRule(var maxLength: Int) : RegionalCoachingMappingRule {
    override fun check(item: RegionalCoachingMapping): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCoachingMappingMinimumRangeRule(
    var min: Double,
    var max: Double,
) : RegionalCoachingMappingRule {
    override fun check(item: RegionalCoachingMapping): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class RegionalCoachingMappingRequiredTitleRule(var message: String) : RegionalCoachingMappingRule {
    override fun check(item: RegionalCoachingMapping): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional coaching mapping. */
data class RegionalCoachingMappingValidator(var rules: MutableList<RegionalCoachingMappingRule>) {
    fun validate(item: RegionalCoachingMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalCoachingMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCoachingMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCoachingMappingValidator(): RegionalCoachingMappingValidator {
    return RegionalCoachingMappingValidator(
        mutableListOf<RegionalCoachingMappingRule>(
            RegionalCoachingMappingRequiredCarrierRule("regional coaching mapping needs a value"),
            RegionalCoachingMappingAllowedTimezoneRule(mutableListOf<String>("regional", "primary")),
            RegionalCoachingMappingTitleLengthRule(120),
            RegionalCoachingMappingMinimumRangeRule(6.2, 4129.2),
            RegionalCoachingMappingRequiredTitleRule("regional coaching mapping needs an entry"),
        ),
    )
}
