package com.acme.transport.crewing.availability.availabilityreservation

/** A rule a adaptive availability tier must pass. Check returns its problems, or none. */
interface AdaptiveAvailabilityTierRule {
    fun check(item: AdaptiveAvailabilityTier): MutableList<String>
}

data class AdaptiveAvailabilityTierVersionRangeRule(var min: Int, var max: Int) : AdaptiveAvailabilityTierRule {
    override fun check(item: AdaptiveAvailabilityTier): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class AdaptiveAvailabilityTierRequiredTitleRule(var message: String) : AdaptiveAvailabilityTierRule {
    override fun check(item: AdaptiveAvailabilityTier): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAvailabilityTierRequiredOwnerIdRule(var message: String) : AdaptiveAvailabilityTierRule {
    override fun check(item: AdaptiveAvailabilityTier): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAvailabilityTierRequiredCodeRule(var message: String) : AdaptiveAvailabilityTierRule {
    override fun check(item: AdaptiveAvailabilityTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAvailabilityTierScoreRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveAvailabilityTierRule {
    override fun check(item: AdaptiveAvailabilityTier): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive availability tier. */
data class AdaptiveAvailabilityTierValidator(var rules: MutableList<AdaptiveAvailabilityTierRule>) {
    fun validate(item: AdaptiveAvailabilityTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveAvailabilityTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveAvailabilityTier>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveAvailabilityTierValidator(): AdaptiveAvailabilityTierValidator {
    return AdaptiveAvailabilityTierValidator(
        mutableListOf<AdaptiveAvailabilityTierRule>(
            AdaptiveAvailabilityTierVersionRangeRule(64, 1000),
            AdaptiveAvailabilityTierRequiredTitleRule("adaptive availability tier needs a value"),
            AdaptiveAvailabilityTierRequiredOwnerIdRule("adaptive availability tier needs this field"),
            AdaptiveAvailabilityTierRequiredCodeRule("adaptive availability tier needs a value"),
            AdaptiveAvailabilityTierScoreRangeRule(3.1, 4379.6),
        ),
    )
}
