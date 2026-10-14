package com.acme.transport.emissions.routeoffset.mapping

/** A rule a regional route offset mapping must pass. Check returns its problems, or none. */
interface RegionalRouteOffsetMappingRule {
    fun check(item: RegionalRouteOffsetMapping): MutableList<String>
}

data class RegionalRouteOffsetMappingCodePrefixRule(var prefix: String) : RegionalRouteOffsetMappingRule {
    override fun check(item: RegionalRouteOffsetMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRouteOffsetMappingDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : RegionalRouteOffsetMappingRule {
    override fun check(item: RegionalRouteOffsetMapping): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRouteOffsetMappingRequiredCodeRule(var message: String) : RegionalRouteOffsetMappingRule {
    override fun check(item: RegionalRouteOffsetMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalRouteOffsetMappingTitleLengthRule(var maxLength: Int) : RegionalRouteOffsetMappingRule {
    override fun check(item: RegionalRouteOffsetMapping): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRouteOffsetMappingExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : RegionalRouteOffsetMappingRule {
    override fun check(item: RegionalRouteOffsetMapping): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class RegionalRouteOffsetMappingCodeLengthRule(var maxLength: Int) : RegionalRouteOffsetMappingRule {
    override fun check(item: RegionalRouteOffsetMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional route offset mapping. */
data class RegionalRouteOffsetMappingValidator(var rules: MutableList<RegionalRouteOffsetMappingRule>) {
    fun validate(item: RegionalRouteOffsetMapping): MutableList<String> {
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

    fun requireValid(item: RegionalRouteOffsetMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalRouteOffsetMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalRouteOffsetMappingValidator(): RegionalRouteOffsetMappingValidator {
    return RegionalRouteOffsetMappingValidator(
        mutableListOf<RegionalRouteOffsetMappingRule>(
            RegionalRouteOffsetMappingCodePrefixRule("REF"),
            RegionalRouteOffsetMappingDistanceKmRangeRule(6.7, 3835.1),
            RegionalRouteOffsetMappingRequiredCodeRule("regional route offset mapping needs this field"),
            RegionalRouteOffsetMappingTitleLengthRule(32),
            RegionalRouteOffsetMappingExpiresAtRangeRule(255, 86400),
            RegionalRouteOffsetMappingCodeLengthRule(32),
        ),
    )
}
