package com.acme.transport.linehaul.backhaul.mapping

import com.acme.transport.kit.text.normalize

/** A rule a strict backhaul mapping must pass. Check returns its problems, or none. */
interface StrictBackhaulMappingRule {
    fun check(item: StrictBackhaulMapping): MutableList<String>
}

data class StrictBackhaulMappingCreatedAtRangeRule(var min: Int, var max: Int) : StrictBackhaulMappingRule {
    override fun check(item: StrictBackhaulMapping): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class StrictBackhaulMappingRequiredTitleRule(var message: String) : StrictBackhaulMappingRule {
    override fun check(item: StrictBackhaulMapping): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictBackhaulMappingAllowedCodeRule(var allowed: MutableList<String>) : StrictBackhaulMappingRule {
    override fun check(item: StrictBackhaulMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class StrictBackhaulMappingRequiredLaneRule(var message: String) : StrictBackhaulMappingRule {
    override fun check(item: StrictBackhaulMapping): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictBackhaulMappingTitleLengthRule(var maxLength: Int) : StrictBackhaulMappingRule {
    override fun check(item: StrictBackhaulMapping): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict backhaul mapping. */
data class StrictBackhaulMappingValidator(var rules: MutableList<StrictBackhaulMappingRule>) {
    fun validate(item: StrictBackhaulMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictBackhaulMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictBackhaulMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictBackhaulMappingValidator(): StrictBackhaulMappingValidator {
    return StrictBackhaulMappingValidator(
        mutableListOf<StrictBackhaulMappingRule>(
            StrictBackhaulMappingCreatedAtRangeRule(1000, 3600),
            StrictBackhaulMappingRequiredTitleRule("strict backhaul mapping needs an entry"),
            StrictBackhaulMappingAllowedCodeRule(mutableListOf<String>("primary", "weighted", "manual")),
            StrictBackhaulMappingRequiredLaneRule("strict backhaul mapping needs this field"),
            StrictBackhaulMappingTitleLengthRule(86400),
        ),
    )
}
