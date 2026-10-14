package com.acme.transport.emissions.routeoffset.entry

/** A rule a route offset schedule must pass. Check returns its problems, or none. */
interface RouteOffsetScheduleRule {
    fun check(item: RouteOffsetSchedule): MutableList<String>
}

data class RouteOffsetScheduleCodePrefixRule(var prefix: String) : RouteOffsetScheduleRule {
    override fun check(item: RouteOffsetSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetScheduleEndsAtRangeRule(var min: Int, var max: Int) : RouteOffsetScheduleRule {
    override fun check(item: RouteOffsetSchedule): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetScheduleTitleLengthRule(var maxLength: Int) : RouteOffsetScheduleRule {
    override fun check(item: RouteOffsetSchedule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetScheduleRequiredChecksumRule(var message: String) : RouteOffsetScheduleRule {
    override fun check(item: RouteOffsetSchedule): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetScheduleChecksumLengthRule(var maxLength: Int) : RouteOffsetScheduleRule {
    override fun check(item: RouteOffsetSchedule): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetScheduleRevisionRangeRule(var min: Int, var max: Int) : RouteOffsetScheduleRule {
    override fun check(item: RouteOffsetSchedule): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset schedule. */
data class RouteOffsetScheduleValidator(var rules: MutableList<RouteOffsetScheduleRule>) {
    fun validate(item: RouteOffsetSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RouteOffsetSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetScheduleValidator(): RouteOffsetScheduleValidator {
    return RouteOffsetScheduleValidator(
        mutableListOf<RouteOffsetScheduleRule>(
            RouteOffsetScheduleCodePrefixRule("REF"),
            RouteOffsetScheduleEndsAtRangeRule(80, 1000),
            RouteOffsetScheduleTitleLengthRule(120),
            RouteOffsetScheduleRequiredChecksumRule("route offset schedule needs an entry"),
            RouteOffsetScheduleChecksumLengthRule(80),
            RouteOffsetScheduleRevisionRangeRule(255, 86400),
        ),
    )
}
