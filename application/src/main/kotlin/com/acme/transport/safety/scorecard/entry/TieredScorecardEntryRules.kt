package com.acme.transport.safety.scorecard.entry

/** A rule a tiered scorecard entry must pass. Check returns its problems, or none. */
interface TieredScorecardEntryRule {
    fun check(item: TieredScorecardEntry): MutableList<String>
}

data class TieredScorecardEntryRetriesRangeRule(var min: Int, var max: Int) : TieredScorecardEntryRule {
    override fun check(item: TieredScorecardEntry): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredScorecardEntryRequiredCodeRule(var message: String) : TieredScorecardEntryRule {
    override fun check(item: TieredScorecardEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredScorecardEntryExternalRefLengthRule(var maxLength: Int) : TieredScorecardEntryRule {
    override fun check(item: TieredScorecardEntry): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered scorecard entry. */
data class TieredScorecardEntryValidator(var rules: MutableList<TieredScorecardEntryRule>) {
    fun validate(item: TieredScorecardEntry): MutableList<String> {
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

    fun requireValid(item: TieredScorecardEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredScorecardEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredScorecardEntryValidator(): TieredScorecardEntryValidator {
    return TieredScorecardEntryValidator(
        mutableListOf<TieredScorecardEntryRule>(
            TieredScorecardEntryRetriesRangeRule(64, 120),
            TieredScorecardEntryRequiredCodeRule("tiered scorecard entry needs a value"),
            TieredScorecardEntryExternalRefLengthRule(3600),
        ),
    )
}
