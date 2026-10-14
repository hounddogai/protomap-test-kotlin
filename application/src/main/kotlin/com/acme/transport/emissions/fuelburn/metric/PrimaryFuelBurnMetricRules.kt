package com.acme.transport.emissions.fuelburn.metric

import com.acme.transport.kit.text.normalize

/** A rule a primary fuel burn metric must pass. Check returns its problems, or none. */
interface PrimaryFuelBurnMetricRule {
    fun check(item: PrimaryFuelBurnMetric): MutableList<String>
}

data class PrimaryFuelBurnMetricChannelLengthRule(var maxLength: Int) : PrimaryFuelBurnMetricRule {
    override fun check(item: PrimaryFuelBurnMetric): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelBurnMetricRequiredCodeRule(var message: String) : PrimaryFuelBurnMetricRule {
    override fun check(item: PrimaryFuelBurnMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelBurnMetricAllowedPhoneRule(var allowed: MutableList<String>) : PrimaryFuelBurnMetricRule {
    override fun check(item: PrimaryFuelBurnMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelBurnMetricGraceDaysRangeRule(var min: Int, var max: Int) : PrimaryFuelBurnMetricRule {
    override fun check(item: PrimaryFuelBurnMetric): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelBurnMetricRequiredChannelRule(var message: String) : PrimaryFuelBurnMetricRule {
    override fun check(item: PrimaryFuelBurnMetric): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelBurnMetricUnitLengthRule(var maxLength: Int) : PrimaryFuelBurnMetricRule {
    override fun check(item: PrimaryFuelBurnMetric): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary fuel burn metric. */
data class PrimaryFuelBurnMetricValidator(var rules: MutableList<PrimaryFuelBurnMetricRule>) {
    fun validate(item: PrimaryFuelBurnMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryFuelBurnMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryFuelBurnMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryFuelBurnMetricValidator(): PrimaryFuelBurnMetricValidator {
    return PrimaryFuelBurnMetricValidator(
        mutableListOf<PrimaryFuelBurnMetricRule>(
            PrimaryFuelBurnMetricChannelLengthRule(3600),
            PrimaryFuelBurnMetricRequiredCodeRule("primary fuel burn metric needs a value"),
            PrimaryFuelBurnMetricAllowedPhoneRule(mutableListOf<String>("rolling", "seasonal", "scheduled")),
            PrimaryFuelBurnMetricGraceDaysRangeRule(1000, 86400),
            PrimaryFuelBurnMetricRequiredChannelRule("primary fuel burn metric needs this field"),
            PrimaryFuelBurnMetricUnitLengthRule(64),
        ),
    )
}
