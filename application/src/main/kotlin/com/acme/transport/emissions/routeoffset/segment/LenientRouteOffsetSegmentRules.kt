package com.acme.transport.emissions.routeoffset.segment

/** A rule a lenient route offset segment must pass. Check returns its problems, or none. */
interface LenientRouteOffsetSegmentRule {
    fun check(item: LenientRouteOffsetSegment): MutableList<String>
}

data class LenientRouteOffsetSegmentCodePrefixRule(var prefix: String) : LenientRouteOffsetSegmentRule {
    override fun check(item: LenientRouteOffsetSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientRouteOffsetSegmentRequiredCodeRule(var message: String) : LenientRouteOffsetSegmentRule {
    override fun check(item: LenientRouteOffsetSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientRouteOffsetSegmentCeilingRangeRule(
    var min: Double,
    var max: Double,
) : LenientRouteOffsetSegmentRule {
    override fun check(item: LenientRouteOffsetSegment): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class LenientRouteOffsetSegmentNameLengthRule(var maxLength: Int) : LenientRouteOffsetSegmentRule {
    override fun check(item: LenientRouteOffsetSegment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient route offset segment. */
data class LenientRouteOffsetSegmentValidator(var rules: MutableList<LenientRouteOffsetSegmentRule>) {
    fun validate(item: LenientRouteOffsetSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientRouteOffsetSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRouteOffsetSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRouteOffsetSegmentValidator(): LenientRouteOffsetSegmentValidator {
    return LenientRouteOffsetSegmentValidator(
        mutableListOf<LenientRouteOffsetSegmentRule>(
            LenientRouteOffsetSegmentCodePrefixRule("Q"),
            LenientRouteOffsetSegmentRequiredCodeRule("lenient route offset segment needs a value"),
            LenientRouteOffsetSegmentCeilingRangeRule(5.9, 403.8),
            LenientRouteOffsetSegmentNameLengthRule(120),
        ),
    )
}
