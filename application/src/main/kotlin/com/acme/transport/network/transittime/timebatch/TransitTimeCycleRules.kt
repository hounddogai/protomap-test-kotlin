package com.acme.transport.network.transittime.timebatch

/** A rule a transit time cycle must pass. Check returns its problems, or none. */
interface TransitTimeCycleRule {
    fun check(item: TransitTimeCycle): MutableList<String>
}

data class TransitTimeCycleRequiredRouteCodeRule(var message: String) : TransitTimeCycleRule {
    override fun check(item: TransitTimeCycle): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeCycleRouteCodeLengthRule(var maxLength: Int) : TransitTimeCycleRule {
    override fun check(item: TransitTimeCycle): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeCycleRequiredCodeRule(var message: String) : TransitTimeCycleRule {
    override fun check(item: TransitTimeCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeCycleVersionRangeRule(var min: Int, var max: Int) : TransitTimeCycleRule {
    override fun check(item: TransitTimeCycle): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeCycleExpiresAtRangeRule(var min: Int, var max: Int) : TransitTimeCycleRule {
    override fun check(item: TransitTimeCycle): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time cycle. */
data class TransitTimeCycleValidator(var rules: MutableList<TransitTimeCycleRule>) {
    fun validate(item: TransitTimeCycle): MutableList<String> {
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

    fun requireValid(item: TransitTimeCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeCycleValidator(): TransitTimeCycleValidator {
    return TransitTimeCycleValidator(
        mutableListOf<TransitTimeCycleRule>(
            TransitTimeCycleRequiredRouteCodeRule("transit time cycle needs this field"),
            TransitTimeCycleRouteCodeLengthRule(80),
            TransitTimeCycleRequiredCodeRule("transit time cycle needs a value"),
            TransitTimeCycleVersionRangeRule(80, 120),
            TransitTimeCycleExpiresAtRangeRule(16, 86400),
        ),
    )
}
