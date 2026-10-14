package com.acme.transport.fleetops.serviceplan.planprofile

/** A rule a legacy service plan metric must pass. Check returns its problems, or none. */
interface LegacyServicePlanMetricRule {
    fun check(item: LegacyServicePlanMetric): MutableList<String>
}

data class LegacyServicePlanMetricFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : LegacyServicePlanMetricRule {
    override fun check(item: LegacyServicePlanMetric): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyServicePlanMetricRequiredLabelRule(var message: String) : LegacyServicePlanMetricRule {
    override fun check(item: LegacyServicePlanMetric): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyServicePlanMetricCodePrefixRule(var prefix: String) : LegacyServicePlanMetricRule {
    override fun check(item: LegacyServicePlanMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyServicePlanMetricTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : LegacyServicePlanMetricRule {
    override fun check(item: LegacyServicePlanMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class LegacyServicePlanMetricRequiredCodeRule(var message: String) : LegacyServicePlanMetricRule {
    override fun check(item: LegacyServicePlanMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyServicePlanMetricCodeLengthRule(var maxLength: Int) : LegacyServicePlanMetricRule {
    override fun check(item: LegacyServicePlanMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy service plan metric. */
data class LegacyServicePlanMetricValidator(var rules: MutableList<LegacyServicePlanMetricRule>) {
    fun validate(item: LegacyServicePlanMetric): MutableList<String> {
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

    fun requireValid(item: LegacyServicePlanMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyServicePlanMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyServicePlanMetricValidator(): LegacyServicePlanMetricValidator {
    return LegacyServicePlanMetricValidator(
        mutableListOf<LegacyServicePlanMetricRule>(
            LegacyServicePlanMetricFuelLitersRangeRule(0.5, 2821.8),
            LegacyServicePlanMetricRequiredLabelRule("legacy service plan metric needs an entry"),
            LegacyServicePlanMetricCodePrefixRule("X"),
            LegacyServicePlanMetricTagsLimitRule(32, mutableListOf<String>()),
            LegacyServicePlanMetricRequiredCodeRule("legacy service plan metric needs this field"),
            LegacyServicePlanMetricCodeLengthRule(3600),
        ),
    )
}
