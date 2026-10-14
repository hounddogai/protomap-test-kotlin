package com.acme.transport.lastmile.locker.lockertier

import com.acme.transport.kit.text.normalize

/** A rule a default locker metric must pass. Check returns its problems, or none. */
interface DefaultLockerMetricRule {
    fun check(item: DefaultLockerMetric): MutableList<String>
}

data class DefaultLockerMetricCodePrefixRule(var prefix: String) : DefaultLockerMetricRule {
    override fun check(item: DefaultLockerMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultLockerMetricRequiredLabelRule(var message: String) : DefaultLockerMetricRule {
    override fun check(item: DefaultLockerMetric): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultLockerMetricAllowedLocaleRule(var allowed: MutableList<String>) : DefaultLockerMetricRule {
    override fun check(item: DefaultLockerMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is expired")
        }
        return mutableListOf<String>()
    }
}

data class DefaultLockerMetricCodeLengthRule(var maxLength: Int) : DefaultLockerMetricRule {
    override fun check(item: DefaultLockerMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default locker metric. */
data class DefaultLockerMetricValidator(var rules: MutableList<DefaultLockerMetricRule>) {
    fun validate(item: DefaultLockerMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultLockerMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultLockerMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultLockerMetricValidator(): DefaultLockerMetricValidator {
    return DefaultLockerMetricValidator(
        mutableListOf<DefaultLockerMetricRule>(
            DefaultLockerMetricCodePrefixRule("Q"),
            DefaultLockerMetricRequiredLabelRule("default locker metric needs an entry"),
            DefaultLockerMetricAllowedLocaleRule(mutableListOf<String>("scheduled", "batched", "pending")),
            DefaultLockerMetricCodeLengthRule(1000),
        ),
    )
}
