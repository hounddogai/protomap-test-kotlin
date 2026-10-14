package com.acme.transport.customs.broker.group

import com.acme.transport.kit.text.normalize

/** A rule a scheduled broker digest must pass. Check returns its problems, or none. */
interface ScheduledBrokerDigestRule {
    fun check(item: ScheduledBrokerDigest): MutableList<String>
}

data class ScheduledBrokerDigestWeightRangeRule(var min: Double, var max: Double) : ScheduledBrokerDigestRule {
    override fun check(item: ScheduledBrokerDigest): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class ScheduledBrokerDigestLabelLengthRule(var maxLength: Int) : ScheduledBrokerDigestRule {
    override fun check(item: ScheduledBrokerDigest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledBrokerDigestCodeLengthRule(var maxLength: Int) : ScheduledBrokerDigestRule {
    override fun check(item: ScheduledBrokerDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledBrokerDigestVisibleGuardRule(var limit: Double) : ScheduledBrokerDigestRule {
    override fun check(item: ScheduledBrokerDigest): MutableList<String> {
        if (item.visible && item.weight > limit) {
            return mutableListOf<String>("weight ${item.weight} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledBrokerDigestAllowedLabelRule(var allowed: MutableList<String>) : ScheduledBrokerDigestRule {
    override fun check(item: ScheduledBrokerDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled broker digest. */
data class ScheduledBrokerDigestValidator(var rules: MutableList<ScheduledBrokerDigestRule>) {
    fun validate(item: ScheduledBrokerDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledBrokerDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledBrokerDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledBrokerDigestValidator(): ScheduledBrokerDigestValidator {
    return ScheduledBrokerDigestValidator(
        mutableListOf<ScheduledBrokerDigestRule>(
            ScheduledBrokerDigestWeightRangeRule(3.3, 3809.9),
            ScheduledBrokerDigestLabelLengthRule(3600),
            ScheduledBrokerDigestCodeLengthRule(64),
            ScheduledBrokerDigestVisibleGuardRule(3.0),
            ScheduledBrokerDigestAllowedLabelRule(mutableListOf<String>("global", "strict", "regional")),
        ),
    )
}
