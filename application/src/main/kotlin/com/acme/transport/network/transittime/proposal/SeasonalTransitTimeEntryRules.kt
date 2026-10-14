package com.acme.transport.network.transittime.proposal

/** A rule a seasonal transit time entry must pass. Check returns its problems, or none. */
interface SeasonalTransitTimeEntryRule {
    fun check(item: SeasonalTransitTimeEntry): MutableList<String>
}

data class SeasonalTransitTimeEntryLabelLengthRule(var maxLength: Int) : SeasonalTransitTimeEntryRule {
    override fun check(item: SeasonalTransitTimeEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTransitTimeEntryRequiredPhoneRule(var message: String) : SeasonalTransitTimeEntryRule {
    override fun check(item: SeasonalTransitTimeEntry): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTransitTimeEntryRequiredCategoryRule(var message: String) : SeasonalTransitTimeEntryRule {
    override fun check(item: SeasonalTransitTimeEntry): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTransitTimeEntryCodePrefixRule(var prefix: String) : SeasonalTransitTimeEntryRule {
    override fun check(item: SeasonalTransitTimeEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTransitTimeEntryBudgetRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalTransitTimeEntryRule {
    override fun check(item: SeasonalTransitTimeEntry): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal transit time entry. */
data class SeasonalTransitTimeEntryValidator(var rules: MutableList<SeasonalTransitTimeEntryRule>) {
    fun validate(item: SeasonalTransitTimeEntry): MutableList<String> {
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

    fun requireValid(item: SeasonalTransitTimeEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTransitTimeEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTransitTimeEntryValidator(): SeasonalTransitTimeEntryValidator {
    return SeasonalTransitTimeEntryValidator(
        mutableListOf<SeasonalTransitTimeEntryRule>(
            SeasonalTransitTimeEntryLabelLengthRule(255),
            SeasonalTransitTimeEntryRequiredPhoneRule("seasonal transit time entry needs an entry"),
            SeasonalTransitTimeEntryRequiredCategoryRule("seasonal transit time entry needs a value"),
            SeasonalTransitTimeEntryCodePrefixRule("X"),
            SeasonalTransitTimeEntryBudgetRangeRule(6.8, 1673.5),
        ),
    )
}
