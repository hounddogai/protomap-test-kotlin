package com.acme.transport.emissions.routeoffset.schedule

/** A rule a fallback route offset schedule must pass. Check returns its problems, or none. */
interface FallbackRouteOffsetScheduleRule {
    fun check(item: FallbackRouteOffsetSchedule): MutableList<String>
}

data class FallbackRouteOffsetScheduleDiscountRangeRule(
    var min: Double,
    var max: Double,
) : FallbackRouteOffsetScheduleRule {
    override fun check(item: FallbackRouteOffsetSchedule): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class FallbackRouteOffsetScheduleNameLengthRule(var maxLength: Int) : FallbackRouteOffsetScheduleRule {
    override fun check(item: FallbackRouteOffsetSchedule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetScheduleCodePrefixRule(var prefix: String) : FallbackRouteOffsetScheduleRule {
    override fun check(item: FallbackRouteOffsetSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetScheduleCodeLengthRule(var maxLength: Int) : FallbackRouteOffsetScheduleRule {
    override fun check(item: FallbackRouteOffsetSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetScheduleRequiredReviewerRule(
    var message: String,
) : FallbackRouteOffsetScheduleRule {
    override fun check(item: FallbackRouteOffsetSchedule): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback route offset schedule. */
data class FallbackRouteOffsetScheduleValidator(var rules: MutableList<FallbackRouteOffsetScheduleRule>) {
    fun validate(item: FallbackRouteOffsetSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackRouteOffsetSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRouteOffsetSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRouteOffsetScheduleValidator(): FallbackRouteOffsetScheduleValidator {
    return FallbackRouteOffsetScheduleValidator(
        mutableListOf<FallbackRouteOffsetScheduleRule>(
            FallbackRouteOffsetScheduleDiscountRangeRule(6.8, 1804.3),
            FallbackRouteOffsetScheduleNameLengthRule(255),
            FallbackRouteOffsetScheduleCodePrefixRule("OPS"),
            FallbackRouteOffsetScheduleCodeLengthRule(1000),
            FallbackRouteOffsetScheduleRequiredReviewerRule("fallback route offset schedule needs an entry"),
        ),
    )
}
