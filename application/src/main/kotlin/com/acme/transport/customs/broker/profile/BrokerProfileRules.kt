package com.acme.transport.customs.broker.profile

import com.acme.transport.kit.text.normalize

/** A rule a broker profile must pass. Check returns its problems, or none. */
interface BrokerProfileRule {
    fun check(item: BrokerProfile): MutableList<String>
}

data class BrokerProfileRequiredRegionRule(var message: String) : BrokerProfileRule {
    override fun check(item: BrokerProfile): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerProfileRegionLengthRule(var maxLength: Int) : BrokerProfileRule {
    override fun check(item: BrokerProfile): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerProfileRequiredTitleRule(var message: String) : BrokerProfileRule {
    override fun check(item: BrokerProfile): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerProfilePriorityRangeRule(var min: Int, var max: Int) : BrokerProfileRule {
    override fun check(item: BrokerProfile): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class BrokerProfileAllowedRegionRule(var allowed: MutableList<String>) : BrokerProfileRule {
    override fun check(item: BrokerProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker profile. */
data class BrokerProfileValidator(var rules: MutableList<BrokerProfileRule>) {
    fun validate(item: BrokerProfile): MutableList<String> {
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

    fun requireValid(item: BrokerProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerProfileValidator(): BrokerProfileValidator {
    return BrokerProfileValidator(
        mutableListOf<BrokerProfileRule>(
            BrokerProfileRequiredRegionRule("broker profile needs this field"),
            BrokerProfileRegionLengthRule(16),
            BrokerProfileRequiredTitleRule("broker profile needs this field"),
            BrokerProfilePriorityRangeRule(3600, 86400),
            BrokerProfileAllowedRegionRule(mutableListOf<String>("fallback", "weighted", "default")),
        ),
    )
}
