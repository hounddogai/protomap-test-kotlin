package com.acme.transport.lastmile.parcel.mapping

/** A rule a parcel mapping must pass. Check returns its problems, or none. */
interface ParcelMappingRule {
    fun check(item: ParcelMapping): MutableList<String>
}

data class ParcelMappingRequiredTitleRule(var message: String) : ParcelMappingRule {
    override fun check(item: ParcelMapping): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelMappingRequiredCodeRule(var message: String) : ParcelMappingRule {
    override fun check(item: ParcelMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelMappingVersionRangeRule(var min: Int, var max: Int) : ParcelMappingRule {
    override fun check(item: ParcelMapping): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ParcelMappingAttemptsRangeRule(var min: Int, var max: Int) : ParcelMappingRule {
    override fun check(item: ParcelMapping): MutableList<String> {
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

data class ParcelMappingRouteCodeLengthRule(var maxLength: Int) : ParcelMappingRule {
    override fun check(item: ParcelMapping): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel mapping. */
data class ParcelMappingValidator(var rules: MutableList<ParcelMappingRule>) {
    fun validate(item: ParcelMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ParcelMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelMappingValidator(): ParcelMappingValidator {
    return ParcelMappingValidator(
        mutableListOf<ParcelMappingRule>(
            ParcelMappingRequiredTitleRule("parcel mapping needs this field"),
            ParcelMappingRequiredCodeRule("parcel mapping needs a value"),
            ParcelMappingVersionRangeRule(80, 1000),
            ParcelMappingAttemptsRangeRule(120, 255),
            ParcelMappingRouteCodeLengthRule(1000),
        ),
    )
}
