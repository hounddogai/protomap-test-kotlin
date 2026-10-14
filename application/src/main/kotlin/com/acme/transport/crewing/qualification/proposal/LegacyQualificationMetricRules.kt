package com.acme.transport.crewing.qualification.proposal

import com.acme.transport.kit.text.normalize

/** A rule a legacy qualification metric must pass. Check returns its problems, or none. */
interface LegacyQualificationMetricRule {
    fun check(item: LegacyQualificationMetric): MutableList<String>
}

data class LegacyQualificationMetricAmountRangeRule(
    var min: Double,
    var max: Double,
) : LegacyQualificationMetricRule {
    override fun check(item: LegacyQualificationMetric): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyQualificationMetricDriverIdLengthRule(var maxLength: Int) : LegacyQualificationMetricRule {
    override fun check(item: LegacyQualificationMetric): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyQualificationMetricRequiredDriverIdRule(var message: String) : LegacyQualificationMetricRule {
    override fun check(item: LegacyQualificationMetric): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyQualificationMetricAllowedLabelRule(
    var allowed: MutableList<String>,
) : LegacyQualificationMetricRule {
    override fun check(item: LegacyQualificationMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class LegacyQualificationMetricCodeLengthRule(var maxLength: Int) : LegacyQualificationMetricRule {
    override fun check(item: LegacyQualificationMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy qualification metric. */
data class LegacyQualificationMetricValidator(var rules: MutableList<LegacyQualificationMetricRule>) {
    fun validate(item: LegacyQualificationMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyQualificationMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyQualificationMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyQualificationMetricValidator(): LegacyQualificationMetricValidator {
    return LegacyQualificationMetricValidator(
        mutableListOf<LegacyQualificationMetricRule>(
            LegacyQualificationMetricAmountRangeRule(0.5, 304.2),
            LegacyQualificationMetricDriverIdLengthRule(16),
            LegacyQualificationMetricRequiredDriverIdRule("legacy qualification metric needs a value"),
            LegacyQualificationMetricAllowedLabelRule(mutableListOf<String>("rolling", "global", "capped")),
            LegacyQualificationMetricCodeLengthRule(3600),
        ),
    )
}
