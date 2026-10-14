package com.acme.transport.emissions.routeoffset.group

/** A rule a capped route offset threshold must pass. Check returns its problems, or none. */
interface CappedRouteOffsetThresholdRule {
    fun check(item: CappedRouteOffsetThreshold): MutableList<String>
}

data class CappedRouteOffsetThresholdNameLengthRule(var maxLength: Int) : CappedRouteOffsetThresholdRule {
    override fun check(item: CappedRouteOffsetThreshold): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRouteOffsetThresholdMaximumRangeRule(
    var min: Double,
    var max: Double,
) : CappedRouteOffsetThresholdRule {
    override fun check(item: CappedRouteOffsetThreshold): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class CappedRouteOffsetThresholdRequiredRegionRule(var message: String) : CappedRouteOffsetThresholdRule {
    override fun check(item: CappedRouteOffsetThreshold): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedRouteOffsetThresholdRegionLengthRule(var maxLength: Int) : CappedRouteOffsetThresholdRule {
    override fun check(item: CappedRouteOffsetThreshold): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRouteOffsetThresholdCodeLengthRule(var maxLength: Int) : CappedRouteOffsetThresholdRule {
    override fun check(item: CappedRouteOffsetThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRouteOffsetThresholdCodePrefixRule(var prefix: String) : CappedRouteOffsetThresholdRule {
    override fun check(item: CappedRouteOffsetThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped route offset threshold. */
data class CappedRouteOffsetThresholdValidator(var rules: MutableList<CappedRouteOffsetThresholdRule>) {
    fun validate(item: CappedRouteOffsetThreshold): MutableList<String> {
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

    fun requireValid(item: CappedRouteOffsetThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedRouteOffsetThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedRouteOffsetThresholdValidator(): CappedRouteOffsetThresholdValidator {
    return CappedRouteOffsetThresholdValidator(
        mutableListOf<CappedRouteOffsetThresholdRule>(
            CappedRouteOffsetThresholdNameLengthRule(1000),
            CappedRouteOffsetThresholdMaximumRangeRule(3.2, 240.0),
            CappedRouteOffsetThresholdRequiredRegionRule("capped route offset threshold needs this field"),
            CappedRouteOffsetThresholdRegionLengthRule(120),
            CappedRouteOffsetThresholdCodeLengthRule(255),
            CappedRouteOffsetThresholdCodePrefixRule("REF"),
        ),
    )
}
