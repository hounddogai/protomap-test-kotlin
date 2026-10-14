package com.acme.transport.customs.broker.threshold

import com.acme.transport.kit.text.normalize

/** A rule a scheduled broker threshold must pass. Check returns its problems, or none. */
interface ScheduledBrokerThresholdRule {
    fun check(item: ScheduledBrokerThreshold): MutableList<String>
}

data class ScheduledBrokerThresholdRequiredNameRule(var message: String) : ScheduledBrokerThresholdRule {
    override fun check(item: ScheduledBrokerThreshold): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledBrokerThresholdGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledBrokerThresholdRule {
    override fun check(item: ScheduledBrokerThreshold): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class ScheduledBrokerThresholdAllowedTitleRule(
    var allowed: MutableList<String>,
) : ScheduledBrokerThresholdRule {
    override fun check(item: ScheduledBrokerThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledBrokerThresholdCodeLengthRule(var maxLength: Int) : ScheduledBrokerThresholdRule {
    override fun check(item: ScheduledBrokerThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledBrokerThresholdCurrencyLengthRule(var maxLength: Int) : ScheduledBrokerThresholdRule {
    override fun check(item: ScheduledBrokerThreshold): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledBrokerThresholdRequiredTitleRule(var message: String) : ScheduledBrokerThresholdRule {
    override fun check(item: ScheduledBrokerThreshold): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled broker threshold. */
data class ScheduledBrokerThresholdValidator(var rules: MutableList<ScheduledBrokerThresholdRule>) {
    fun validate(item: ScheduledBrokerThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledBrokerThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledBrokerThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledBrokerThresholdValidator(): ScheduledBrokerThresholdValidator {
    return ScheduledBrokerThresholdValidator(
        mutableListOf<ScheduledBrokerThresholdRule>(
            ScheduledBrokerThresholdRequiredNameRule("scheduled broker threshold needs this field"),
            ScheduledBrokerThresholdGraceDaysRangeRule(80, 86400),
            ScheduledBrokerThresholdAllowedTitleRule(mutableListOf<String>("scheduled")),
            ScheduledBrokerThresholdCodeLengthRule(32),
            ScheduledBrokerThresholdCurrencyLengthRule(80),
            ScheduledBrokerThresholdRequiredTitleRule("scheduled broker threshold needs an entry"),
        ),
    )
}
