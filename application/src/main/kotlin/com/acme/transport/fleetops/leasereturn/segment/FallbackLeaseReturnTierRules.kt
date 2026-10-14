package com.acme.transport.fleetops.leasereturn.segment

/** A rule a fallback lease return tier must pass. Check returns its problems, or none. */
interface FallbackLeaseReturnTierRule {
    fun check(item: FallbackLeaseReturnTier): MutableList<String>
}

data class FallbackLeaseReturnTierRouteCodeLengthRule(var maxLength: Int) : FallbackLeaseReturnTierRule {
    override fun check(item: FallbackLeaseReturnTier): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLeaseReturnTierVehicleIdLengthRule(var maxLength: Int) : FallbackLeaseReturnTierRule {
    override fun check(item: FallbackLeaseReturnTier): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLeaseReturnTierNameLengthRule(var maxLength: Int) : FallbackLeaseReturnTierRule {
    override fun check(item: FallbackLeaseReturnTier): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLeaseReturnTierScoreRangeRule(
    var min: Double,
    var max: Double,
) : FallbackLeaseReturnTierRule {
    override fun check(item: FallbackLeaseReturnTier): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLeaseReturnTierPriorityRangeRule(var min: Int, var max: Int) : FallbackLeaseReturnTierRule {
    override fun check(item: FallbackLeaseReturnTier): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class FallbackLeaseReturnTierMaximumRangeRule(
    var min: Double,
    var max: Double,
) : FallbackLeaseReturnTierRule {
    override fun check(item: FallbackLeaseReturnTier): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback lease return tier. */
data class FallbackLeaseReturnTierValidator(var rules: MutableList<FallbackLeaseReturnTierRule>) {
    fun validate(item: FallbackLeaseReturnTier): MutableList<String> {
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

    fun requireValid(item: FallbackLeaseReturnTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackLeaseReturnTier>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackLeaseReturnTierValidator(): FallbackLeaseReturnTierValidator {
    return FallbackLeaseReturnTierValidator(
        mutableListOf<FallbackLeaseReturnTierRule>(
            FallbackLeaseReturnTierRouteCodeLengthRule(3600),
            FallbackLeaseReturnTierVehicleIdLengthRule(120),
            FallbackLeaseReturnTierNameLengthRule(255),
            FallbackLeaseReturnTierScoreRangeRule(0.1, 3292.5),
            FallbackLeaseReturnTierPriorityRangeRule(64, 3600),
            FallbackLeaseReturnTierMaximumRangeRule(6.5, 1720.4),
        ),
    )
}
