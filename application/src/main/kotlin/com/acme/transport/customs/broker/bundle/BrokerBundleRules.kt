package com.acme.transport.customs.broker.bundle

import com.acme.transport.kit.text.normalize

/** A rule a broker bundle must pass. Check returns its problems, or none. */
interface BrokerBundleRule {
    fun check(item: BrokerBundle): MutableList<String>
}

data class BrokerBundleAllowedCarrierRule(var allowed: MutableList<String>) : BrokerBundleRule {
    override fun check(item: BrokerBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class BrokerBundleCreatedAtRangeRule(var min: Int, var max: Int) : BrokerBundleRule {
    override fun check(item: BrokerBundle): MutableList<String> {
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

data class BrokerBundleNameLengthRule(var maxLength: Int) : BrokerBundleRule {
    override fun check(item: BrokerBundle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker bundle. */
data class BrokerBundleValidator(var rules: MutableList<BrokerBundleRule>) {
    fun validate(item: BrokerBundle): MutableList<String> {
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

    fun requireValid(item: BrokerBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerBundleValidator(): BrokerBundleValidator {
    return BrokerBundleValidator(
        mutableListOf<BrokerBundleRule>(
            BrokerBundleAllowedCarrierRule(mutableListOf<String>("manual", "seasonal", "rolling")),
            BrokerBundleCreatedAtRangeRule(255, 86400),
            BrokerBundleNameLengthRule(64),
        ),
    )
}
