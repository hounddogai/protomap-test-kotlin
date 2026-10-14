package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.kit.text.normalize

/** A rule a default parcel metric must pass. Check returns its problems, or none. */
interface DefaultParcelMetricRule {
    fun check(item: DefaultParcelMetric): MutableList<String>
}

data class DefaultParcelMetricCodePrefixRule(var prefix: String) : DefaultParcelMetricRule {
    override fun check(item: DefaultParcelMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelMetricAllowedSegmentRule(var allowed: MutableList<String>) : DefaultParcelMetricRule {
    override fun check(item: DefaultParcelMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelMetricCodeLengthRule(var maxLength: Int) : DefaultParcelMetricRule {
    override fun check(item: DefaultParcelMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelMetricTitleLengthRule(var maxLength: Int) : DefaultParcelMetricRule {
    override fun check(item: DefaultParcelMetric): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default parcel metric. */
data class DefaultParcelMetricValidator(var rules: MutableList<DefaultParcelMetricRule>) {
    fun validate(item: DefaultParcelMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultParcelMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultParcelMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultParcelMetricValidator(): DefaultParcelMetricValidator {
    return DefaultParcelMetricValidator(
        mutableListOf<DefaultParcelMetricRule>(
            DefaultParcelMetricCodePrefixRule("ACM"),
            DefaultParcelMetricAllowedSegmentRule(mutableListOf<String>("scheduled", "regional", "tiered")),
            DefaultParcelMetricCodeLengthRule(64),
            DefaultParcelMetricTitleLengthRule(32),
        ),
    )
}
