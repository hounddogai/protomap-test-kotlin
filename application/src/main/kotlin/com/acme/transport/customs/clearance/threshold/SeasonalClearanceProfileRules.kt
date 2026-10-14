package com.acme.transport.customs.clearance.threshold

/** A rule a seasonal clearance profile must pass. Check returns its problems, or none. */
interface SeasonalClearanceProfileRule {
    fun check(item: SeasonalClearanceProfile): MutableList<String>
}

data class SeasonalClearanceProfileEndsAtRangeRule(var min: Int, var max: Int) : SeasonalClearanceProfileRule {
    override fun check(item: SeasonalClearanceProfile): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalClearanceProfileRetriesRangeRule(var min: Int, var max: Int) : SeasonalClearanceProfileRule {
    override fun check(item: SeasonalClearanceProfile): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalClearanceProfileRequiredCodeRule(var message: String) : SeasonalClearanceProfileRule {
    override fun check(item: SeasonalClearanceProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalClearanceProfilePhoneLengthRule(var maxLength: Int) : SeasonalClearanceProfileRule {
    override fun check(item: SeasonalClearanceProfile): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalClearanceProfileCodePrefixRule(var prefix: String) : SeasonalClearanceProfileRule {
    override fun check(item: SeasonalClearanceProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalClearanceProfileRequiredLabelRule(var message: String) : SeasonalClearanceProfileRule {
    override fun check(item: SeasonalClearanceProfile): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal clearance profile. */
data class SeasonalClearanceProfileValidator(var rules: MutableList<SeasonalClearanceProfileRule>) {
    fun validate(item: SeasonalClearanceProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalClearanceProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalClearanceProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalClearanceProfileValidator(): SeasonalClearanceProfileValidator {
    return SeasonalClearanceProfileValidator(
        mutableListOf<SeasonalClearanceProfileRule>(
            SeasonalClearanceProfileEndsAtRangeRule(32, 64),
            SeasonalClearanceProfileRetriesRangeRule(32, 80),
            SeasonalClearanceProfileRequiredCodeRule("seasonal clearance profile needs a value"),
            SeasonalClearanceProfilePhoneLengthRule(64),
            SeasonalClearanceProfileCodePrefixRule("X"),
            SeasonalClearanceProfileRequiredLabelRule("seasonal clearance profile needs a value"),
        ),
    )
}
