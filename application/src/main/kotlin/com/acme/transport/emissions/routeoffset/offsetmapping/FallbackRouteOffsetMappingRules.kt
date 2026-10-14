package com.acme.transport.emissions.routeoffset.offsetmapping

/** A rule a fallback route offset mapping must pass. Check returns its problems, or none. */
interface FallbackRouteOffsetMappingRule {
    fun check(item: FallbackRouteOffsetMapping): MutableList<String>
}

data class FallbackRouteOffsetMappingCodePrefixRule(var prefix: String) : FallbackRouteOffsetMappingRule {
    override fun check(item: FallbackRouteOffsetMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetMappingLabelLengthRule(var maxLength: Int) : FallbackRouteOffsetMappingRule {
    override fun check(item: FallbackRouteOffsetMapping): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetMappingUnitLengthRule(var maxLength: Int) : FallbackRouteOffsetMappingRule {
    override fun check(item: FallbackRouteOffsetMapping): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetMappingCodeLengthRule(var maxLength: Int) : FallbackRouteOffsetMappingRule {
    override fun check(item: FallbackRouteOffsetMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetMappingRequiredUnitRule(var message: String) : FallbackRouteOffsetMappingRule {
    override fun check(item: FallbackRouteOffsetMapping): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback route offset mapping. */
data class FallbackRouteOffsetMappingValidator(var rules: MutableList<FallbackRouteOffsetMappingRule>) {
    fun validate(item: FallbackRouteOffsetMapping): MutableList<String> {
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

    fun requireValid(item: FallbackRouteOffsetMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRouteOffsetMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRouteOffsetMappingValidator(): FallbackRouteOffsetMappingValidator {
    return FallbackRouteOffsetMappingValidator(
        mutableListOf<FallbackRouteOffsetMappingRule>(
            FallbackRouteOffsetMappingCodePrefixRule("ACM"),
            FallbackRouteOffsetMappingLabelLengthRule(16),
            FallbackRouteOffsetMappingUnitLengthRule(86400),
            FallbackRouteOffsetMappingCodeLengthRule(86400),
            FallbackRouteOffsetMappingRequiredUnitRule("fallback route offset mapping needs an entry"),
        ),
    )
}
