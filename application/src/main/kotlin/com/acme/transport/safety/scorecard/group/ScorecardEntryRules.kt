package com.acme.transport.safety.scorecard.group

/** A rule a scorecard entry must pass. Check returns its problems, or none. */
interface ScorecardEntryRule {
    fun check(item: ScorecardEntry): MutableList<String>
}

data class ScorecardEntryDiscountRangeRule(var min: Double, var max: Double) : ScorecardEntryRule {
    override fun check(item: ScorecardEntry): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardEntryEtaMinutesRangeRule(var min: Int, var max: Int) : ScorecardEntryRule {
    override fun check(item: ScorecardEntry): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class ScorecardEntryRegionLengthRule(var maxLength: Int) : ScorecardEntryRule {
    override fun check(item: ScorecardEntry): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardEntryLocaleLengthRule(var maxLength: Int) : ScorecardEntryRule {
    override fun check(item: ScorecardEntry): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard entry. */
data class ScorecardEntryValidator(var rules: MutableList<ScorecardEntryRule>) {
    fun validate(item: ScorecardEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScorecardEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardEntryValidator(): ScorecardEntryValidator {
    return ScorecardEntryValidator(
        mutableListOf<ScorecardEntryRule>(
            ScorecardEntryDiscountRangeRule(8.6, 2150.8),
            ScorecardEntryEtaMinutesRangeRule(80, 3600),
            ScorecardEntryRegionLengthRule(120),
            ScorecardEntryLocaleLengthRule(86400),
        ),
    )
}
