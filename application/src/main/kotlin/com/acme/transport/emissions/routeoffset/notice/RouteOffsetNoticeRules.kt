package com.acme.transport.emissions.routeoffset.notice

/** A rule a route offset notice must pass. Check returns its problems, or none. */
interface RouteOffsetNoticeRule {
    fun check(item: RouteOffsetNotice): MutableList<String>
}

data class RouteOffsetNoticeCodePrefixRule(var prefix: String) : RouteOffsetNoticeRule {
    override fun check(item: RouteOffsetNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetNoticeRequiredLabelRule(var message: String) : RouteOffsetNoticeRule {
    override fun check(item: RouteOffsetNotice): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetNoticeDiscountRangeRule(var min: Double, var max: Double) : RouteOffsetNoticeRule {
    override fun check(item: RouteOffsetNotice): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetNoticeLabelLengthRule(var maxLength: Int) : RouteOffsetNoticeRule {
    override fun check(item: RouteOffsetNotice): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetNoticeRequiredCodeRule(var message: String) : RouteOffsetNoticeRule {
    override fun check(item: RouteOffsetNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetNoticeAttemptsRangeRule(var min: Int, var max: Int) : RouteOffsetNoticeRule {
    override fun check(item: RouteOffsetNotice): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a route offset notice. */
data class RouteOffsetNoticeValidator(var rules: MutableList<RouteOffsetNoticeRule>) {
    fun validate(item: RouteOffsetNotice): MutableList<String> {
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

    fun requireValid(item: RouteOffsetNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetNoticeValidator(): RouteOffsetNoticeValidator {
    return RouteOffsetNoticeValidator(
        mutableListOf<RouteOffsetNoticeRule>(
            RouteOffsetNoticeCodePrefixRule("X"),
            RouteOffsetNoticeRequiredLabelRule("route offset notice needs an entry"),
            RouteOffsetNoticeDiscountRangeRule(9.1, 3838.7),
            RouteOffsetNoticeLabelLengthRule(86400),
            RouteOffsetNoticeRequiredCodeRule("route offset notice needs a value"),
            RouteOffsetNoticeAttemptsRangeRule(255, 1000),
        ),
    )
}
