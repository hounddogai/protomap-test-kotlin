package com.acme.transport.customs.broker.estimate

import com.acme.transport.kit.text.normalize

/** A rule a broker quota must pass. Check returns its problems, or none. */
interface BrokerQuotaRule {
    fun check(item: BrokerQuota): MutableList<String>
}

data class BrokerQuotaWindowDaysRangeRule(var min: Int, var max: Int) : BrokerQuotaRule {
    override fun check(item: BrokerQuota): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class BrokerQuotaAllowedNameRule(var allowed: MutableList<String>) : BrokerQuotaRule {
    override fun check(item: BrokerQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class BrokerQuotaSequenceRangeRule(var min: Int, var max: Int) : BrokerQuotaRule {
    override fun check(item: BrokerQuota): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BrokerQuotaRegionLengthRule(var maxLength: Int) : BrokerQuotaRule {
    override fun check(item: BrokerQuota): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerQuotaRequiredCodeRule(var message: String) : BrokerQuotaRule {
    override fun check(item: BrokerQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker quota. */
data class BrokerQuotaValidator(var rules: MutableList<BrokerQuotaRule>) {
    fun validate(item: BrokerQuota): MutableList<String> {
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

    fun requireValid(item: BrokerQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerQuotaValidator(): BrokerQuotaValidator {
    return BrokerQuotaValidator(
        mutableListOf<BrokerQuotaRule>(
            BrokerQuotaWindowDaysRangeRule(32, 80),
            BrokerQuotaAllowedNameRule(mutableListOf<String>()),
            BrokerQuotaSequenceRangeRule(1000, 3600),
            BrokerQuotaRegionLengthRule(1000),
            BrokerQuotaRequiredCodeRule("broker quota needs an entry"),
        ),
    )
}
