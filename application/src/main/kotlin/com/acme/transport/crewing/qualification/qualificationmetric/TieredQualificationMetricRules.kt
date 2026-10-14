package com.acme.transport.crewing.qualification.qualificationmetric

/** A rule a tiered qualification metric must pass. Check returns its problems, or none. */
interface TieredQualificationMetricRule {
    fun check(item: TieredQualificationMetric): MutableList<String>
}

data class TieredQualificationMetricCodePrefixRule(var prefix: String) : TieredQualificationMetricRule {
    override fun check(item: TieredQualificationMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredQualificationMetricThresholdRangeRule(
    var min: Double,
    var max: Double,
) : TieredQualificationMetricRule {
    override fun check(item: TieredQualificationMetric): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredQualificationMetricChannelLengthRule(var maxLength: Int) : TieredQualificationMetricRule {
    override fun check(item: TieredQualificationMetric): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredQualificationMetricSourceLengthRule(var maxLength: Int) : TieredQualificationMetricRule {
    override fun check(item: TieredQualificationMetric): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered qualification metric. */
data class TieredQualificationMetricValidator(var rules: MutableList<TieredQualificationMetricRule>) {
    fun validate(item: TieredQualificationMetric): MutableList<String> {
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

    fun requireValid(item: TieredQualificationMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredQualificationMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredQualificationMetricValidator(): TieredQualificationMetricValidator {
    return TieredQualificationMetricValidator(
        mutableListOf<TieredQualificationMetricRule>(
            TieredQualificationMetricCodePrefixRule("Q"),
            TieredQualificationMetricThresholdRangeRule(6.2, 256.5),
            TieredQualificationMetricChannelLengthRule(16),
            TieredQualificationMetricSourceLengthRule(64),
        ),
    )
}
