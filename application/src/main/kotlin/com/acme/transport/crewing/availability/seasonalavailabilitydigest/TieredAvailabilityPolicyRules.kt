package com.acme.transport.crewing.availability.seasonalavailabilitydigest

/** A rule a tiered availability policy must pass. Check returns its problems, or none. */
interface TieredAvailabilityPolicyRule {
    fun check(item: TieredAvailabilityPolicy): MutableList<String>
}

data class TieredAvailabilityPolicyFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : TieredAvailabilityPolicyRule {
    override fun check(item: TieredAvailabilityPolicy): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredAvailabilityPolicyTitleLengthRule(var maxLength: Int) : TieredAvailabilityPolicyRule {
    override fun check(item: TieredAvailabilityPolicy): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredAvailabilityPolicyRequiredCodeRule(var message: String) : TieredAvailabilityPolicyRule {
    override fun check(item: TieredAvailabilityPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredAvailabilityPolicyEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : TieredAvailabilityPolicyRule {
    override fun check(item: TieredAvailabilityPolicy): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered availability policy. */
data class TieredAvailabilityPolicyValidator(var rules: MutableList<TieredAvailabilityPolicyRule>) {
    fun validate(item: TieredAvailabilityPolicy): MutableList<String> {
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

    fun requireValid(item: TieredAvailabilityPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredAvailabilityPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredAvailabilityPolicyValidator(): TieredAvailabilityPolicyValidator {
    return TieredAvailabilityPolicyValidator(
        mutableListOf<TieredAvailabilityPolicyRule>(
            TieredAvailabilityPolicyFuelLitersRangeRule(5.5, 2710.4),
            TieredAvailabilityPolicyTitleLengthRule(120),
            TieredAvailabilityPolicyRequiredCodeRule("tiered availability policy needs an entry"),
            TieredAvailabilityPolicyEtaMinutesRangeRule(16, 120),
        ),
    )
}
