package com.acme.transport.customs.broker.reservation

import com.acme.transport.kit.text.normalize

/** A rule a broker digest must pass. Check returns its problems, or none. */
interface BrokerDigestRule {
    fun check(item: BrokerDigest): MutableList<String>
}

data class BrokerDigestDurationMinutesRangeRule(var min: Int, var max: Int) : BrokerDigestRule {
    override fun check(item: BrokerDigest): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BrokerDigestAllowedLabelRule(var allowed: MutableList<String>) : BrokerDigestRule {
    override fun check(item: BrokerDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is too long")
        }
        return mutableListOf<String>()
    }
}

data class BrokerDigestStopCountRangeRule(var min: Int, var max: Int) : BrokerDigestRule {
    override fun check(item: BrokerDigest): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BrokerDigestLoadKgRangeRule(var min: Double, var max: Double) : BrokerDigestRule {
    override fun check(item: BrokerDigest): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BrokerDigestLimitRangeRule(var min: Int, var max: Int) : BrokerDigestRule {
    override fun check(item: BrokerDigest): MutableList<String> {
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

/** Runs every rule of a broker digest. */
data class BrokerDigestValidator(var rules: MutableList<BrokerDigestRule>) {
    fun validate(item: BrokerDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BrokerDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerDigestValidator(): BrokerDigestValidator {
    return BrokerDigestValidator(
        mutableListOf<BrokerDigestRule>(
            BrokerDigestDurationMinutesRangeRule(32, 120),
            BrokerDigestAllowedLabelRule(mutableListOf<String>("tiered", "rolling", "regional")),
            BrokerDigestStopCountRangeRule(64, 120),
            BrokerDigestLoadKgRangeRule(8.3, 2374.8),
            BrokerDigestLimitRangeRule(64, 80),
        ),
    )
}
