package com.acme.transport.emissions.routeoffset.exception

/** A rule a lenient route offset exception must pass. Check returns its problems, or none. */
interface LenientRouteOffsetExceptionRule {
    fun check(item: LenientRouteOffsetException): MutableList<String>
}

data class LenientRouteOffsetExceptionCapacityRangeRule(
    var min: Int,
    var max: Int,
) : LenientRouteOffsetExceptionRule {
    override fun check(item: LenientRouteOffsetException): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class LenientRouteOffsetExceptionCodeLengthRule(var maxLength: Int) : LenientRouteOffsetExceptionRule {
    override fun check(item: LenientRouteOffsetException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRouteOffsetExceptionCodePrefixRule(var prefix: String) : LenientRouteOffsetExceptionRule {
    override fun check(item: LenientRouteOffsetException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientRouteOffsetExceptionPalletsRangeRule(
    var min: Int,
    var max: Int,
) : LenientRouteOffsetExceptionRule {
    override fun check(item: LenientRouteOffsetException): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientRouteOffsetExceptionLabelLengthRule(var maxLength: Int) : LenientRouteOffsetExceptionRule {
    override fun check(item: LenientRouteOffsetException): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient route offset exception. */
data class LenientRouteOffsetExceptionValidator(var rules: MutableList<LenientRouteOffsetExceptionRule>) {
    fun validate(item: LenientRouteOffsetException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientRouteOffsetException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRouteOffsetException>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRouteOffsetExceptionValidator(): LenientRouteOffsetExceptionValidator {
    return LenientRouteOffsetExceptionValidator(
        mutableListOf<LenientRouteOffsetExceptionRule>(
            LenientRouteOffsetExceptionCapacityRangeRule(32, 120),
            LenientRouteOffsetExceptionCodeLengthRule(1000),
            LenientRouteOffsetExceptionCodePrefixRule("X"),
            LenientRouteOffsetExceptionPalletsRangeRule(120, 86400),
            LenientRouteOffsetExceptionLabelLengthRule(32),
        ),
    )
}
