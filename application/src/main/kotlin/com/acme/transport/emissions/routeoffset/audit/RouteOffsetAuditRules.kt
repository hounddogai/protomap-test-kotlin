package com.acme.transport.emissions.routeoffset.audit

/** A rule a route offset audit must pass. Check returns its problems, or none. */
interface RouteOffsetAuditRule {
    fun check(item: RouteOffsetAudit): MutableList<String>
}

data class RouteOffsetAuditRequiredCodeRule(var message: String) : RouteOffsetAuditRule {
    override fun check(item: RouteOffsetAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetAuditOdometerRangeRule(var min: Int, var max: Int) : RouteOffsetAuditRule {
    override fun check(item: RouteOffsetAudit): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetAuditRetriesRangeRule(var min: Int, var max: Int) : RouteOffsetAuditRule {
    override fun check(item: RouteOffsetAudit): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetAuditCodePrefixRule(var prefix: String) : RouteOffsetAuditRule {
    override fun check(item: RouteOffsetAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset audit. */
data class RouteOffsetAuditValidator(var rules: MutableList<RouteOffsetAuditRule>) {
    fun validate(item: RouteOffsetAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RouteOffsetAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetAuditValidator(): RouteOffsetAuditValidator {
    return RouteOffsetAuditValidator(
        mutableListOf<RouteOffsetAuditRule>(
            RouteOffsetAuditRequiredCodeRule("route offset audit needs this field"),
            RouteOffsetAuditOdometerRangeRule(80, 255),
            RouteOffsetAuditRetriesRangeRule(16, 1000),
            RouteOffsetAuditCodePrefixRule("X"),
        ),
    )
}
