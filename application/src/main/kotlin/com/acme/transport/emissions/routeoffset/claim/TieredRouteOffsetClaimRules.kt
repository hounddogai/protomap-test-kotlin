package com.acme.transport.emissions.routeoffset.claim

/** A rule a tiered route offset claim must pass. Check returns its problems, or none. */
interface TieredRouteOffsetClaimRule {
    fun check(item: TieredRouteOffsetClaim): MutableList<String>
}

data class TieredRouteOffsetClaimCodePrefixRule(var prefix: String) : TieredRouteOffsetClaimRule {
    override fun check(item: TieredRouteOffsetClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredRouteOffsetClaimRequiredSegmentRule(var message: String) : TieredRouteOffsetClaimRule {
    override fun check(item: TieredRouteOffsetClaim): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredRouteOffsetClaimOdometerRangeRule(var min: Int, var max: Int) : TieredRouteOffsetClaimRule {
    override fun check(item: TieredRouteOffsetClaim): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class TieredRouteOffsetClaimSegmentLengthRule(var maxLength: Int) : TieredRouteOffsetClaimRule {
    override fun check(item: TieredRouteOffsetClaim): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredRouteOffsetClaimEndsAtRangeRule(var min: Int, var max: Int) : TieredRouteOffsetClaimRule {
    override fun check(item: TieredRouteOffsetClaim): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tiered route offset claim. */
data class TieredRouteOffsetClaimValidator(var rules: MutableList<TieredRouteOffsetClaimRule>) {
    fun validate(item: TieredRouteOffsetClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredRouteOffsetClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredRouteOffsetClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredRouteOffsetClaimValidator(): TieredRouteOffsetClaimValidator {
    return TieredRouteOffsetClaimValidator(
        mutableListOf<TieredRouteOffsetClaimRule>(
            TieredRouteOffsetClaimCodePrefixRule("ACM"),
            TieredRouteOffsetClaimRequiredSegmentRule("tiered route offset claim needs this field"),
            TieredRouteOffsetClaimOdometerRangeRule(255, 255),
            TieredRouteOffsetClaimSegmentLengthRule(16),
            TieredRouteOffsetClaimEndsAtRangeRule(16, 255),
        ),
    )
}
