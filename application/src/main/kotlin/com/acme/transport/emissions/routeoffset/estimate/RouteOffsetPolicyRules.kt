package com.acme.transport.emissions.routeoffset.estimate

/** A rule a route offset policy must pass. Check returns its problems, or none. */
interface RouteOffsetPolicyRule {
    fun check(item: RouteOffsetPolicy): MutableList<String>
}

data class RouteOffsetPolicyRequiredNameRule(var message: String) : RouteOffsetPolicyRule {
    override fun check(item: RouteOffsetPolicy): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetPolicyRequiredCodeRule(var message: String) : RouteOffsetPolicyRule {
    override fun check(item: RouteOffsetPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetPolicyNameLengthRule(var maxLength: Int) : RouteOffsetPolicyRule {
    override fun check(item: RouteOffsetPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetPolicyRevisionRangeRule(var min: Int, var max: Int) : RouteOffsetPolicyRule {
    override fun check(item: RouteOffsetPolicy): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetPolicyMaximumRangeRule(var min: Double, var max: Double) : RouteOffsetPolicyRule {
    override fun check(item: RouteOffsetPolicy): MutableList<String> {
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

data class RouteOffsetPolicySourceLengthRule(var maxLength: Int) : RouteOffsetPolicyRule {
    override fun check(item: RouteOffsetPolicy): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset policy. */
data class RouteOffsetPolicyValidator(var rules: MutableList<RouteOffsetPolicyRule>) {
    fun validate(item: RouteOffsetPolicy): MutableList<String> {
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

    fun requireValid(item: RouteOffsetPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetPolicyValidator(): RouteOffsetPolicyValidator {
    return RouteOffsetPolicyValidator(
        mutableListOf<RouteOffsetPolicyRule>(
            RouteOffsetPolicyRequiredNameRule("route offset policy needs an entry"),
            RouteOffsetPolicyRequiredCodeRule("route offset policy needs a value"),
            RouteOffsetPolicyNameLengthRule(32),
            RouteOffsetPolicyRevisionRangeRule(32, 1000),
            RouteOffsetPolicyMaximumRangeRule(8.9, 713.1),
            RouteOffsetPolicySourceLengthRule(255),
        ),
    )
}
