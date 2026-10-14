package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.kit.text.normalize

/** A rule a route offset review must pass. Check returns its problems, or none. */
interface RouteOffsetReviewRule {
    fun check(item: RouteOffsetReview): MutableList<String>
}

data class RouteOffsetReviewMaximumRangeRule(var min: Double, var max: Double) : RouteOffsetReviewRule {
    override fun check(item: RouteOffsetReview): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class RouteOffsetReviewCountryLengthRule(var maxLength: Int) : RouteOffsetReviewRule {
    override fun check(item: RouteOffsetReview): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetReviewLoadKgRangeRule(var min: Double, var max: Double) : RouteOffsetReviewRule {
    override fun check(item: RouteOffsetReview): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class RouteOffsetReviewNameLengthRule(var maxLength: Int) : RouteOffsetReviewRule {
    override fun check(item: RouteOffsetReview): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetReviewAllowedCountryRule(var allowed: MutableList<String>) : RouteOffsetReviewRule {
    override fun check(item: RouteOffsetReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetReviewRequiredNameRule(var message: String) : RouteOffsetReviewRule {
    override fun check(item: RouteOffsetReview): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset review. */
data class RouteOffsetReviewValidator(var rules: MutableList<RouteOffsetReviewRule>) {
    fun validate(item: RouteOffsetReview): MutableList<String> {
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

    fun requireValid(item: RouteOffsetReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetReview>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetReviewValidator(): RouteOffsetReviewValidator {
    return RouteOffsetReviewValidator(
        mutableListOf<RouteOffsetReviewRule>(
            RouteOffsetReviewMaximumRangeRule(7.3, 3444.5),
            RouteOffsetReviewCountryLengthRule(86400),
            RouteOffsetReviewLoadKgRangeRule(6.6, 3265.8),
            RouteOffsetReviewNameLengthRule(32),
            RouteOffsetReviewAllowedCountryRule(mutableListOf<String>("seasonal", "adaptive", "fallback")),
            RouteOffsetReviewRequiredNameRule("route offset review needs this field"),
        ),
    )
}
