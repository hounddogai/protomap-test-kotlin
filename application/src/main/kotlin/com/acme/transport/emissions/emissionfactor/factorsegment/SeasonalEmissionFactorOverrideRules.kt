package com.acme.transport.emissions.emissionfactor.factorsegment

/** A rule a seasonal emission factor override must pass. Check returns its problems, or none. */
interface SeasonalEmissionFactorOverrideRule {
    fun check(item: SeasonalEmissionFactorOverride): MutableList<String>
}

data class SeasonalEmissionFactorOverrideSegmentLengthRule(
    var maxLength: Int,
) : SeasonalEmissionFactorOverrideRule {
    override fun check(item: SeasonalEmissionFactorOverride): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEmissionFactorOverrideCurrencyLengthRule(
    var maxLength: Int,
) : SeasonalEmissionFactorOverrideRule {
    override fun check(item: SeasonalEmissionFactorOverride): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEmissionFactorOverrideRequiredLabelRule(
    var message: String,
) : SeasonalEmissionFactorOverrideRule {
    override fun check(item: SeasonalEmissionFactorOverride): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal emission factor override. */
data class SeasonalEmissionFactorOverrideValidator(var rules: MutableList<SeasonalEmissionFactorOverrideRule>) {
    fun validate(item: SeasonalEmissionFactorOverride): MutableList<String> {
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

    fun requireValid(item: SeasonalEmissionFactorOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalEmissionFactorOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalEmissionFactorOverrideValidator(): SeasonalEmissionFactorOverrideValidator {
    return SeasonalEmissionFactorOverrideValidator(
        mutableListOf<SeasonalEmissionFactorOverrideRule>(
            SeasonalEmissionFactorOverrideSegmentLengthRule(120),
            SeasonalEmissionFactorOverrideCurrencyLengthRule(3600),
            SeasonalEmissionFactorOverrideRequiredLabelRule("seasonal emission factor override needs an entry"),
        ),
    )
}
