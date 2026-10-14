package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.kit.text.normalize

/** A rule a route offset quota must pass. Check returns its problems, or none. */
interface RouteOffsetQuotaRule {
    fun check(item: RouteOffsetQuota): MutableList<String>
}

data class RouteOffsetQuotaCodePrefixRule(var prefix: String) : RouteOffsetQuotaRule {
    override fun check(item: RouteOffsetQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetQuotaAllowedCodeRule(var allowed: MutableList<String>) : RouteOffsetQuotaRule {
    override fun check(item: RouteOffsetQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetQuotaVersionRangeRule(var min: Int, var max: Int) : RouteOffsetQuotaRule {
    override fun check(item: RouteOffsetQuota): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class RouteOffsetQuotaRequiredLabelRule(var message: String) : RouteOffsetQuotaRule {
    override fun check(item: RouteOffsetQuota): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset quota. */
data class RouteOffsetQuotaValidator(var rules: MutableList<RouteOffsetQuotaRule>) {
    fun validate(item: RouteOffsetQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RouteOffsetQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetQuotaValidator(): RouteOffsetQuotaValidator {
    return RouteOffsetQuotaValidator(
        mutableListOf<RouteOffsetQuotaRule>(
            RouteOffsetQuotaCodePrefixRule("REF"),
            RouteOffsetQuotaAllowedCodeRule(mutableListOf<String>()),
            RouteOffsetQuotaVersionRangeRule(64, 120),
            RouteOffsetQuotaRequiredLabelRule("route offset quota needs this field"),
        ),
    )
}
