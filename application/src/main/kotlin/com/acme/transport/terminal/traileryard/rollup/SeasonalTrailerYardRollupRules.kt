package com.acme.transport.terminal.traileryard.rollup

/** A rule a seasonal trailer yard rollup must pass. Check returns its problems, or none. */
interface SeasonalTrailerYardRollupRule {
    fun check(item: SeasonalTrailerYardRollup): MutableList<String>
}

data class SeasonalTrailerYardRollupCodeLengthRule(var maxLength: Int) : SeasonalTrailerYardRollupRule {
    override fun check(item: SeasonalTrailerYardRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardRollupArchivedGuardRule(var limit: Double) : SeasonalTrailerYardRollupRule {
    override fun check(item: SeasonalTrailerYardRollup): MutableList<String> {
        if (item.archived && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardRollupRequiredDockRule(var message: String) : SeasonalTrailerYardRollupRule {
    override fun check(item: SeasonalTrailerYardRollup): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardRollupRequiredLabelRule(var message: String) : SeasonalTrailerYardRollupRule {
    override fun check(item: SeasonalTrailerYardRollup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardRollupRequiredCodeRule(var message: String) : SeasonalTrailerYardRollupRule {
    override fun check(item: SeasonalTrailerYardRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal trailer yard rollup. */
data class SeasonalTrailerYardRollupValidator(var rules: MutableList<SeasonalTrailerYardRollupRule>) {
    fun validate(item: SeasonalTrailerYardRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalTrailerYardRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTrailerYardRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTrailerYardRollupValidator(): SeasonalTrailerYardRollupValidator {
    return SeasonalTrailerYardRollupValidator(
        mutableListOf<SeasonalTrailerYardRollupRule>(
            SeasonalTrailerYardRollupCodeLengthRule(32),
            SeasonalTrailerYardRollupArchivedGuardRule(0.7),
            SeasonalTrailerYardRollupRequiredDockRule("seasonal trailer yard rollup needs this field"),
            SeasonalTrailerYardRollupRequiredLabelRule("seasonal trailer yard rollup needs a value"),
            SeasonalTrailerYardRollupRequiredCodeRule("seasonal trailer yard rollup needs a value"),
        ),
    )
}
