package com.acme.transport.terminal.crossdock.regionaldockclaim

/** A rule a weighted cross dock claim must pass. Check returns its problems, or none. */
interface WeightedCrossDockClaimRule {
    fun check(item: WeightedCrossDockClaim): MutableList<String>
}

data class WeightedCrossDockClaimRequiredVehicleIdRule(var message: String) : WeightedCrossDockClaimRule {
    override fun check(item: WeightedCrossDockClaim): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedCrossDockClaimCeilingRangeRule(
    var min: Double,
    var max: Double,
) : WeightedCrossDockClaimRule {
    override fun check(item: WeightedCrossDockClaim): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCrossDockClaimVehicleIdLengthRule(var maxLength: Int) : WeightedCrossDockClaimRule {
    override fun check(item: WeightedCrossDockClaim): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCrossDockClaimVisibleGuardRule(var limit: Double) : WeightedCrossDockClaimRule {
    override fun check(item: WeightedCrossDockClaim): MutableList<String> {
        if (item.visible && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted cross dock claim. */
data class WeightedCrossDockClaimValidator(var rules: MutableList<WeightedCrossDockClaimRule>) {
    fun validate(item: WeightedCrossDockClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedCrossDockClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedCrossDockClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedCrossDockClaimValidator(): WeightedCrossDockClaimValidator {
    return WeightedCrossDockClaimValidator(
        mutableListOf<WeightedCrossDockClaimRule>(
            WeightedCrossDockClaimRequiredVehicleIdRule("weighted cross dock claim needs this field"),
            WeightedCrossDockClaimCeilingRangeRule(3.1, 2835.5),
            WeightedCrossDockClaimVehicleIdLengthRule(16),
            WeightedCrossDockClaimVisibleGuardRule(3.5),
        ),
    )
}
