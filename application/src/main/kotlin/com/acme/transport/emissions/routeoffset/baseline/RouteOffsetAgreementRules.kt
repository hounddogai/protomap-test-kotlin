package com.acme.transport.emissions.routeoffset.baseline

/** A rule a route offset agreement must pass. Check returns its problems, or none. */
interface RouteOffsetAgreementRule {
    fun check(item: RouteOffsetAgreement): MutableList<String>
}

data class RouteOffsetAgreementGraceDaysRangeRule(var min: Int, var max: Int) : RouteOffsetAgreementRule {
    override fun check(item: RouteOffsetAgreement): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class RouteOffsetAgreementRequiredVehicleIdRule(var message: String) : RouteOffsetAgreementRule {
    override fun check(item: RouteOffsetAgreement): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetAgreementRequiredDescriptionRule(var message: String) : RouteOffsetAgreementRule {
    override fun check(item: RouteOffsetAgreement): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetAgreementTitleLengthRule(var maxLength: Int) : RouteOffsetAgreementRule {
    override fun check(item: RouteOffsetAgreement): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset agreement. */
data class RouteOffsetAgreementValidator(var rules: MutableList<RouteOffsetAgreementRule>) {
    fun validate(item: RouteOffsetAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RouteOffsetAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetAgreementValidator(): RouteOffsetAgreementValidator {
    return RouteOffsetAgreementValidator(
        mutableListOf<RouteOffsetAgreementRule>(
            RouteOffsetAgreementGraceDaysRangeRule(80, 120),
            RouteOffsetAgreementRequiredVehicleIdRule("route offset agreement needs a value"),
            RouteOffsetAgreementRequiredDescriptionRule("route offset agreement needs a value"),
            RouteOffsetAgreementTitleLengthRule(16),
        ),
    )
}
