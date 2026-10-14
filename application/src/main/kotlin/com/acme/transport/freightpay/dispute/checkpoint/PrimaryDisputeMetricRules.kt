package com.acme.transport.freightpay.dispute.checkpoint

/** A rule a primary dispute metric must pass. Check returns its problems, or none. */
interface PrimaryDisputeMetricRule {
    fun check(item: PrimaryDisputeMetric): MutableList<String>
}

data class PrimaryDisputeMetricDurationMinutesRangeRule(var min: Int, var max: Int) : PrimaryDisputeMetricRule {
    override fun check(item: PrimaryDisputeMetric): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class PrimaryDisputeMetricStopCountRangeRule(var min: Int, var max: Int) : PrimaryDisputeMetricRule {
    override fun check(item: PrimaryDisputeMetric): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class PrimaryDisputeMetricSourceLengthRule(var maxLength: Int) : PrimaryDisputeMetricRule {
    override fun check(item: PrimaryDisputeMetric): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDisputeMetricLimitRangeRule(var min: Int, var max: Int) : PrimaryDisputeMetricRule {
    override fun check(item: PrimaryDisputeMetric): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDisputeMetricCodePrefixRule(var prefix: String) : PrimaryDisputeMetricRule {
    override fun check(item: PrimaryDisputeMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary dispute metric. */
data class PrimaryDisputeMetricValidator(var rules: MutableList<PrimaryDisputeMetricRule>) {
    fun validate(item: PrimaryDisputeMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryDisputeMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryDisputeMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryDisputeMetricValidator(): PrimaryDisputeMetricValidator {
    return PrimaryDisputeMetricValidator(
        mutableListOf<PrimaryDisputeMetricRule>(
            PrimaryDisputeMetricDurationMinutesRangeRule(120, 86400),
            PrimaryDisputeMetricStopCountRangeRule(80, 80),
            PrimaryDisputeMetricSourceLengthRule(64),
            PrimaryDisputeMetricLimitRangeRule(120, 3600),
            PrimaryDisputeMetricCodePrefixRule("ACM"),
        ),
    )
}
