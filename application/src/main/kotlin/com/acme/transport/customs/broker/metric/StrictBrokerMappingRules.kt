package com.acme.transport.customs.broker.metric

import com.acme.transport.kit.text.normalize

/** A rule a strict broker mapping must pass. Check returns its problems, or none. */
interface StrictBrokerMappingRule {
    fun check(item: StrictBrokerMapping): MutableList<String>
}

data class StrictBrokerMappingRequiredLabelRule(var message: String) : StrictBrokerMappingRule {
    override fun check(item: StrictBrokerMapping): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictBrokerMappingLimitRangeRule(var min: Int, var max: Int) : StrictBrokerMappingRule {
    override fun check(item: StrictBrokerMapping): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class StrictBrokerMappingAllowedUnitRule(var allowed: MutableList<String>) : StrictBrokerMappingRule {
    override fun check(item: StrictBrokerMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is stale")
        }
        return mutableListOf<String>()
    }
}

data class StrictBrokerMappingCodeLengthRule(var maxLength: Int) : StrictBrokerMappingRule {
    override fun check(item: StrictBrokerMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictBrokerMappingRequiredSegmentRule(var message: String) : StrictBrokerMappingRule {
    override fun check(item: StrictBrokerMapping): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictBrokerMappingRequiredSummaryRule(var message: String) : StrictBrokerMappingRule {
    override fun check(item: StrictBrokerMapping): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict broker mapping. */
data class StrictBrokerMappingValidator(var rules: MutableList<StrictBrokerMappingRule>) {
    fun validate(item: StrictBrokerMapping): MutableList<String> {
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

    fun requireValid(item: StrictBrokerMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictBrokerMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictBrokerMappingValidator(): StrictBrokerMappingValidator {
    return StrictBrokerMappingValidator(
        mutableListOf<StrictBrokerMappingRule>(
            StrictBrokerMappingRequiredLabelRule("strict broker mapping needs a value"),
            StrictBrokerMappingLimitRangeRule(80, 86400),
            StrictBrokerMappingAllowedUnitRule(mutableListOf<String>("seasonal")),
            StrictBrokerMappingCodeLengthRule(3600),
            StrictBrokerMappingRequiredSegmentRule("strict broker mapping needs an entry"),
            StrictBrokerMappingRequiredSummaryRule("strict broker mapping needs a value"),
        ),
    )
}
