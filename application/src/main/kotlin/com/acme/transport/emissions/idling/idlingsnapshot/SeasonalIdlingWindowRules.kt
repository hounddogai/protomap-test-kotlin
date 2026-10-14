package com.acme.transport.emissions.idling.idlingsnapshot

/** A rule a seasonal idling window must pass. Check returns its problems, or none. */
interface SeasonalIdlingWindowRule {
    fun check(item: SeasonalIdlingWindow): MutableList<String>
}

data class SeasonalIdlingWindowRequiredLabelRule(var message: String) : SeasonalIdlingWindowRule {
    override fun check(item: SeasonalIdlingWindow): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalIdlingWindowRequiredLocaleRule(var message: String) : SeasonalIdlingWindowRule {
    override fun check(item: SeasonalIdlingWindow): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalIdlingWindowCategoryLengthRule(var maxLength: Int) : SeasonalIdlingWindowRule {
    override fun check(item: SeasonalIdlingWindow): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal idling window. */
data class SeasonalIdlingWindowValidator(var rules: MutableList<SeasonalIdlingWindowRule>) {
    fun validate(item: SeasonalIdlingWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalIdlingWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalIdlingWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalIdlingWindowValidator(): SeasonalIdlingWindowValidator {
    return SeasonalIdlingWindowValidator(
        mutableListOf<SeasonalIdlingWindowRule>(
            SeasonalIdlingWindowRequiredLabelRule("seasonal idling window needs a value"),
            SeasonalIdlingWindowRequiredLocaleRule("seasonal idling window needs an entry"),
            SeasonalIdlingWindowCategoryLengthRule(3600),
        ),
    )
}
