package com.acme.transport.linehaul.tender.metric

import com.acme.transport.kit.text.normalize

/** A rule a default tender metric must pass. Check returns its problems, or none. */
interface DefaultTenderMetricRule {
    fun check(item: DefaultTenderMetric): MutableList<String>
}

data class DefaultTenderMetricAmountRangeRule(var min: Double, var max: Double) : DefaultTenderMetricRule {
    override fun check(item: DefaultTenderMetric): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTenderMetricAllowedTimezoneRule(var allowed: MutableList<String>) : DefaultTenderMetricRule {
    override fun check(item: DefaultTenderMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTenderMetricLabelLengthRule(var maxLength: Int) : DefaultTenderMetricRule {
    override fun check(item: DefaultTenderMetric): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTenderMetricCodePrefixRule(var prefix: String) : DefaultTenderMetricRule {
    override fun check(item: DefaultTenderMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTenderMetricTimezoneLengthRule(var maxLength: Int) : DefaultTenderMetricRule {
    override fun check(item: DefaultTenderMetric): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default tender metric. */
data class DefaultTenderMetricValidator(var rules: MutableList<DefaultTenderMetricRule>) {
    fun validate(item: DefaultTenderMetric): MutableList<String> {
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

    fun requireValid(item: DefaultTenderMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTenderMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTenderMetricValidator(): DefaultTenderMetricValidator {
    return DefaultTenderMetricValidator(
        mutableListOf<DefaultTenderMetricRule>(
            DefaultTenderMetricAmountRangeRule(2.0, 2557.1),
            DefaultTenderMetricAllowedTimezoneRule(mutableListOf<String>("rolling")),
            DefaultTenderMetricLabelLengthRule(32),
            DefaultTenderMetricCodePrefixRule("Q"),
            DefaultTenderMetricTimezoneLengthRule(80),
        ),
    )
}
