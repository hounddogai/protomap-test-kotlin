package com.acme.transport.terminal.dockdoor.estimate

/** A rule a seasonal dock door estimate must pass. Check returns its problems, or none. */
interface SeasonalDockDoorEstimateRule {
    fun check(item: SeasonalDockDoorEstimate): MutableList<String>
}

data class SeasonalDockDoorEstimatePinnedGuardRule(var limit: Double) : SeasonalDockDoorEstimateRule {
    override fun check(item: SeasonalDockDoorEstimate): MutableList<String> {
        if (item.pinned && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDockDoorEstimateRequiredTitleRule(var message: String) : SeasonalDockDoorEstimateRule {
    override fun check(item: SeasonalDockDoorEstimate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDockDoorEstimateRequiredRegionRule(var message: String) : SeasonalDockDoorEstimateRule {
    override fun check(item: SeasonalDockDoorEstimate): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDockDoorEstimateCodePrefixRule(var prefix: String) : SeasonalDockDoorEstimateRule {
    override fun check(item: SeasonalDockDoorEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDockDoorEstimateTitleLengthRule(var maxLength: Int) : SeasonalDockDoorEstimateRule {
    override fun check(item: SeasonalDockDoorEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal dock door estimate. */
data class SeasonalDockDoorEstimateValidator(var rules: MutableList<SeasonalDockDoorEstimateRule>) {
    fun validate(item: SeasonalDockDoorEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalDockDoorEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDockDoorEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDockDoorEstimateValidator(): SeasonalDockDoorEstimateValidator {
    return SeasonalDockDoorEstimateValidator(
        mutableListOf<SeasonalDockDoorEstimateRule>(
            SeasonalDockDoorEstimatePinnedGuardRule(4.2),
            SeasonalDockDoorEstimateRequiredTitleRule("seasonal dock door estimate needs an entry"),
            SeasonalDockDoorEstimateRequiredRegionRule("seasonal dock door estimate needs this field"),
            SeasonalDockDoorEstimateCodePrefixRule("OPS"),
            SeasonalDockDoorEstimateTitleLengthRule(1000),
        ),
    )
}
