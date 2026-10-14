package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.kit.text.normalize

/** A rule a weighted route offset quota must pass. Check returns its problems, or none. */
interface WeightedRouteOffsetQuotaRule {
    fun check(item: WeightedRouteOffsetQuota): MutableList<String>
}

data class WeightedRouteOffsetQuotaDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : WeightedRouteOffsetQuotaRule {
    override fun check(item: WeightedRouteOffsetQuota): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class WeightedRouteOffsetQuotaRequiredLabelRule(var message: String) : WeightedRouteOffsetQuotaRule {
    override fun check(item: WeightedRouteOffsetQuota): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedRouteOffsetQuotaPhoneLengthRule(var maxLength: Int) : WeightedRouteOffsetQuotaRule {
    override fun check(item: WeightedRouteOffsetQuota): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRouteOffsetQuotaAllowedLabelRule(
    var allowed: MutableList<String>,
) : WeightedRouteOffsetQuotaRule {
    override fun check(item: WeightedRouteOffsetQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted route offset quota. */
data class WeightedRouteOffsetQuotaValidator(var rules: MutableList<WeightedRouteOffsetQuotaRule>) {
    fun validate(item: WeightedRouteOffsetQuota): MutableList<String> {
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

    fun requireValid(item: WeightedRouteOffsetQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedRouteOffsetQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedRouteOffsetQuotaValidator(): WeightedRouteOffsetQuotaValidator {
    return WeightedRouteOffsetQuotaValidator(
        mutableListOf<WeightedRouteOffsetQuotaRule>(
            WeightedRouteOffsetQuotaDistanceKmRangeRule(7.2, 1319.8),
            WeightedRouteOffsetQuotaRequiredLabelRule("weighted route offset quota needs a value"),
            WeightedRouteOffsetQuotaPhoneLengthRule(255),
            WeightedRouteOffsetQuotaAllowedLabelRule(mutableListOf<String>("lenient")),
        ),
    )
}
