package com.acme.transport.lastmile.redelivery.metric

import com.acme.transport.kit.text.normalize

/** A rule a redelivery metric must pass. Check returns its problems, or none. */
interface RedeliveryMetricRule {
    fun check(item: RedeliveryMetric): MutableList<String>
}

data class RedeliveryMetricRequiredTitleRule(var message: String) : RedeliveryMetricRule {
    override fun check(item: RedeliveryMetric): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryMetricAllowedCodeRule(var allowed: MutableList<String>) : RedeliveryMetricRule {
    override fun check(item: RedeliveryMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryMetricTitleLengthRule(var maxLength: Int) : RedeliveryMetricRule {
    override fun check(item: RedeliveryMetric): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryMetricCodePrefixRule(var prefix: String) : RedeliveryMetricRule {
    override fun check(item: RedeliveryMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery metric. */
data class RedeliveryMetricValidator(var rules: MutableList<RedeliveryMetricRule>) {
    fun validate(item: RedeliveryMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RedeliveryMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryMetricValidator(): RedeliveryMetricValidator {
    return RedeliveryMetricValidator(
        mutableListOf<RedeliveryMetricRule>(
            RedeliveryMetricRequiredTitleRule("redelivery metric needs this field"),
            RedeliveryMetricAllowedCodeRule(mutableListOf<String>("adaptive", "manual", "legacy")),
            RedeliveryMetricTitleLengthRule(120),
            RedeliveryMetricCodePrefixRule("REF"),
        ),
    )
}
