package com.acme.transport.freightpay.accessorial.accessorialmetric

/** A rule a seasonal accessorial score must pass. Check returns its problems, or none. */
interface SeasonalAccessorialScoreRule {
    fun check(item: SeasonalAccessorialScore): MutableList<String>
}

data class SeasonalAccessorialScoreLabelLengthRule(var maxLength: Int) : SeasonalAccessorialScoreRule {
    override fun check(item: SeasonalAccessorialScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAccessorialScoreRequiredLabelRule(var message: String) : SeasonalAccessorialScoreRule {
    override fun check(item: SeasonalAccessorialScore): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAccessorialScoreRequiredNameRule(var message: String) : SeasonalAccessorialScoreRule {
    override fun check(item: SeasonalAccessorialScore): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal accessorial score. */
data class SeasonalAccessorialScoreValidator(var rules: MutableList<SeasonalAccessorialScoreRule>) {
    fun validate(item: SeasonalAccessorialScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalAccessorialScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalAccessorialScore>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalAccessorialScoreValidator(): SeasonalAccessorialScoreValidator {
    return SeasonalAccessorialScoreValidator(
        mutableListOf<SeasonalAccessorialScoreRule>(
            SeasonalAccessorialScoreLabelLengthRule(16),
            SeasonalAccessorialScoreRequiredLabelRule("seasonal accessorial score needs a value"),
            SeasonalAccessorialScoreRequiredNameRule("seasonal accessorial score needs this field"),
        ),
    )
}
