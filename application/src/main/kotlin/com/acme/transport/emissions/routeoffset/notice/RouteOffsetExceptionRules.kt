package com.acme.transport.emissions.routeoffset.notice

/** A rule a route offset exception must pass. Check returns its problems, or none. */
interface RouteOffsetExceptionRule {
    fun check(item: RouteOffsetException): MutableList<String>
}

data class RouteOffsetExceptionStartsAtRangeRule(var min: Int, var max: Int) : RouteOffsetExceptionRule {
    override fun check(item: RouteOffsetException): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class RouteOffsetExceptionMinimumRangeRule(var min: Double, var max: Double) : RouteOffsetExceptionRule {
    override fun check(item: RouteOffsetException): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetExceptionCodeLengthRule(var maxLength: Int) : RouteOffsetExceptionRule {
    override fun check(item: RouteOffsetException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetExceptionCodePrefixRule(var prefix: String) : RouteOffsetExceptionRule {
    override fun check(item: RouteOffsetException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetExceptionChecksumLengthRule(var maxLength: Int) : RouteOffsetExceptionRule {
    override fun check(item: RouteOffsetException): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetExceptionRequiredCodeRule(var message: String) : RouteOffsetExceptionRule {
    override fun check(item: RouteOffsetException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset exception. */
data class RouteOffsetExceptionValidator(var rules: MutableList<RouteOffsetExceptionRule>) {
    fun validate(item: RouteOffsetException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RouteOffsetException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetException>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetExceptionValidator(): RouteOffsetExceptionValidator {
    return RouteOffsetExceptionValidator(
        mutableListOf<RouteOffsetExceptionRule>(
            RouteOffsetExceptionStartsAtRangeRule(64, 255),
            RouteOffsetExceptionMinimumRangeRule(1.9, 3375.9),
            RouteOffsetExceptionCodeLengthRule(3600),
            RouteOffsetExceptionCodePrefixRule("ACM"),
            RouteOffsetExceptionChecksumLengthRule(32),
            RouteOffsetExceptionRequiredCodeRule("route offset exception needs an entry"),
        ),
    )
}
