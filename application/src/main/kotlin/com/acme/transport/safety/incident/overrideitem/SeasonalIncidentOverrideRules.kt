package com.acme.transport.safety.incident.overrideitem

/** A rule a seasonal incident override must pass. Check returns its problems, or none. */
interface SeasonalIncidentOverrideRule {
    fun check(item: SeasonalIncidentOverride): MutableList<String>
}

data class SeasonalIncidentOverrideRequiredCountryRule(var message: String) : SeasonalIncidentOverrideRule {
    override fun check(item: SeasonalIncidentOverride): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalIncidentOverrideEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalIncidentOverrideRule {
    override fun check(item: SeasonalIncidentOverride): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalIncidentOverrideTimezoneLengthRule(var maxLength: Int) : SeasonalIncidentOverrideRule {
    override fun check(item: SeasonalIncidentOverride): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal incident override. */
data class SeasonalIncidentOverrideValidator(var rules: MutableList<SeasonalIncidentOverrideRule>) {
    fun validate(item: SeasonalIncidentOverride): MutableList<String> {
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

    fun requireValid(item: SeasonalIncidentOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalIncidentOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalIncidentOverrideValidator(): SeasonalIncidentOverrideValidator {
    return SeasonalIncidentOverrideValidator(
        mutableListOf<SeasonalIncidentOverrideRule>(
            SeasonalIncidentOverrideRequiredCountryRule("seasonal incident override needs an entry"),
            SeasonalIncidentOverrideEtaMinutesRangeRule(64, 80),
            SeasonalIncidentOverrideTimezoneLengthRule(255),
        ),
    )
}
