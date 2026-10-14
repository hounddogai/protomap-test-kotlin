package com.acme.transport.network.transittime.timedigest

import com.acme.transport.kit.text.normalize

/** A rule a weighted transit time digest must pass. Check returns its problems, or none. */
interface WeightedTransitTimeDigestRule {
    fun check(item: WeightedTransitTimeDigest): MutableList<String>
}

data class WeightedTransitTimeDigestAllowedLocaleRule(
    var allowed: MutableList<String>,
) : WeightedTransitTimeDigestRule {
    override fun check(item: WeightedTransitTimeDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is stale")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTransitTimeDigestRequiredLocaleRule(var message: String) : WeightedTransitTimeDigestRule {
    override fun check(item: WeightedTransitTimeDigest): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedTransitTimeDigestCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : WeightedTransitTimeDigestRule {
    override fun check(item: WeightedTransitTimeDigest): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted transit time digest. */
data class WeightedTransitTimeDigestValidator(var rules: MutableList<WeightedTransitTimeDigestRule>) {
    fun validate(item: WeightedTransitTimeDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedTransitTimeDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedTransitTimeDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedTransitTimeDigestValidator(): WeightedTransitTimeDigestValidator {
    return WeightedTransitTimeDigestValidator(
        mutableListOf<WeightedTransitTimeDigestRule>(
            WeightedTransitTimeDigestAllowedLocaleRule(mutableListOf<String>("global", "batched")),
            WeightedTransitTimeDigestRequiredLocaleRule("weighted transit time digest needs a value"),
            WeightedTransitTimeDigestCreatedAtRangeRule(120, 255),
        ),
    )
}
