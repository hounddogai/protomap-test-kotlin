package com.acme.transport.emissions.emissionfactor.factortarget

/** A rule a adaptive emission factor milestone must pass. Check returns its problems, or none. */
interface AdaptiveEmissionFactorMilestoneRule {
    fun check(item: AdaptiveEmissionFactorMilestone): MutableList<String>
}

data class AdaptiveEmissionFactorMilestoneVersionRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveEmissionFactorMilestoneRule {
    override fun check(item: AdaptiveEmissionFactorMilestone): MutableList<String> {
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

data class AdaptiveEmissionFactorMilestoneRequiredCurrencyRule(
    var message: String,
) : AdaptiveEmissionFactorMilestoneRule {
    override fun check(item: AdaptiveEmissionFactorMilestone): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorMilestoneCurrencyLengthRule(
    var maxLength: Int,
) : AdaptiveEmissionFactorMilestoneRule {
    override fun check(item: AdaptiveEmissionFactorMilestone): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive emission factor milestone. */
data class AdaptiveEmissionFactorMilestoneValidator(
    var rules: MutableList<AdaptiveEmissionFactorMilestoneRule>,
) {
    fun validate(item: AdaptiveEmissionFactorMilestone): MutableList<String> {
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

    fun requireValid(item: AdaptiveEmissionFactorMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveEmissionFactorMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveEmissionFactorMilestoneValidator(): AdaptiveEmissionFactorMilestoneValidator {
    return AdaptiveEmissionFactorMilestoneValidator(
        mutableListOf<AdaptiveEmissionFactorMilestoneRule>(
            AdaptiveEmissionFactorMilestoneVersionRangeRule(120, 86400),
            AdaptiveEmissionFactorMilestoneRequiredCurrencyRule(
                "adaptive emission factor milestone needs this field",
            ),
            AdaptiveEmissionFactorMilestoneCurrencyLengthRule(16),
        ),
    )
}
