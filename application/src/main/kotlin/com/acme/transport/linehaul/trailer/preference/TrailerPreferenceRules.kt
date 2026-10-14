package com.acme.transport.linehaul.trailer.preference

/** A rule a trailer preference must pass. Check returns its problems, or none. */
interface TrailerPreferenceRule {
    fun check(item: TrailerPreference): MutableList<String>
}

data class TrailerPreferenceGraceDaysRangeRule(var min: Int, var max: Int) : TrailerPreferenceRule {
    override fun check(item: TrailerPreference): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerPreferenceRequiredLabelRule(var message: String) : TrailerPreferenceRule {
    override fun check(item: TrailerPreference): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerPreferenceRequiredReviewerRule(var message: String) : TrailerPreferenceRule {
    override fun check(item: TrailerPreference): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer preference. */
data class TrailerPreferenceValidator(var rules: MutableList<TrailerPreferenceRule>) {
    fun validate(item: TrailerPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerPreferenceValidator(): TrailerPreferenceValidator {
    return TrailerPreferenceValidator(
        mutableListOf<TrailerPreferenceRule>(
            TrailerPreferenceGraceDaysRangeRule(64, 3600),
            TrailerPreferenceRequiredLabelRule("trailer preference needs an entry"),
            TrailerPreferenceRequiredReviewerRule("trailer preference needs this field"),
        ),
    )
}
