package com.acme.transport.emissions.routeoffset.estimate

/** A rule a seasonal route offset claim must pass. Check returns its problems, or none. */
interface SeasonalRouteOffsetClaimRule {
    fun check(item: SeasonalRouteOffsetClaim): MutableList<String>
}

data class SeasonalRouteOffsetClaimRequiredCodeRule(var message: String) : SeasonalRouteOffsetClaimRule {
    override fun check(item: SeasonalRouteOffsetClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRouteOffsetClaimDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalRouteOffsetClaimRule {
    override fun check(item: SeasonalRouteOffsetClaim): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class SeasonalRouteOffsetClaimRequiredDepotRule(var message: String) : SeasonalRouteOffsetClaimRule {
    override fun check(item: SeasonalRouteOffsetClaim): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRouteOffsetClaimDepotLengthRule(var maxLength: Int) : SeasonalRouteOffsetClaimRule {
    override fun check(item: SeasonalRouteOffsetClaim): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRouteOffsetClaimCodePrefixRule(var prefix: String) : SeasonalRouteOffsetClaimRule {
    override fun check(item: SeasonalRouteOffsetClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal route offset claim. */
data class SeasonalRouteOffsetClaimValidator(var rules: MutableList<SeasonalRouteOffsetClaimRule>) {
    fun validate(item: SeasonalRouteOffsetClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalRouteOffsetClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRouteOffsetClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRouteOffsetClaimValidator(): SeasonalRouteOffsetClaimValidator {
    return SeasonalRouteOffsetClaimValidator(
        mutableListOf<SeasonalRouteOffsetClaimRule>(
            SeasonalRouteOffsetClaimRequiredCodeRule("seasonal route offset claim needs a value"),
            SeasonalRouteOffsetClaimDistanceKmRangeRule(0.7, 2737.1),
            SeasonalRouteOffsetClaimRequiredDepotRule("seasonal route offset claim needs this field"),
            SeasonalRouteOffsetClaimDepotLengthRule(3600),
            SeasonalRouteOffsetClaimCodePrefixRule("REF"),
        ),
    )
}
