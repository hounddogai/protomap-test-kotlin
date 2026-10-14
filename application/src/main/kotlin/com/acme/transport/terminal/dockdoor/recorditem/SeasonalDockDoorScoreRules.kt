package com.acme.transport.terminal.dockdoor.recorditem

/** A rule a seasonal dock door score must pass. Check returns its problems, or none. */
interface SeasonalDockDoorScoreRule {
    fun check(item: SeasonalDockDoorScore): MutableList<String>
}

data class SeasonalDockDoorScoreRequiredCodeRule(var message: String) : SeasonalDockDoorScoreRule {
    override fun check(item: SeasonalDockDoorScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDockDoorScoreCodePrefixRule(var prefix: String) : SeasonalDockDoorScoreRule {
    override fun check(item: SeasonalDockDoorScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDockDoorScoreSummaryLengthRule(var maxLength: Int) : SeasonalDockDoorScoreRule {
    override fun check(item: SeasonalDockDoorScore): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal dock door score. */
data class SeasonalDockDoorScoreValidator(var rules: MutableList<SeasonalDockDoorScoreRule>) {
    fun validate(item: SeasonalDockDoorScore): MutableList<String> {
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

    fun requireValid(item: SeasonalDockDoorScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDockDoorScore>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDockDoorScoreValidator(): SeasonalDockDoorScoreValidator {
    return SeasonalDockDoorScoreValidator(
        mutableListOf<SeasonalDockDoorScoreRule>(
            SeasonalDockDoorScoreRequiredCodeRule("seasonal dock door score needs an entry"),
            SeasonalDockDoorScoreCodePrefixRule("OPS"),
            SeasonalDockDoorScoreSummaryLengthRule(86400),
        ),
    )
}
