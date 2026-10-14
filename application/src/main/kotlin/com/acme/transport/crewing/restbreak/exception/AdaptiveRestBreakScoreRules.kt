package com.acme.transport.crewing.restbreak.exception

/** A rule a adaptive rest break score must pass. Check returns its problems, or none. */
interface AdaptiveRestBreakScoreRule {
    fun check(item: AdaptiveRestBreakScore): MutableList<String>
}

data class AdaptiveRestBreakScoreRequiredLaneRule(var message: String) : AdaptiveRestBreakScoreRule {
    override fun check(item: AdaptiveRestBreakScore): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRestBreakScoreAttemptsRangeRule(var min: Int, var max: Int) : AdaptiveRestBreakScoreRule {
    override fun check(item: AdaptiveRestBreakScore): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRestBreakScoreDescriptionLengthRule(var maxLength: Int) : AdaptiveRestBreakScoreRule {
    override fun check(item: AdaptiveRestBreakScore): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRestBreakScoreCodeLengthRule(var maxLength: Int) : AdaptiveRestBreakScoreRule {
    override fun check(item: AdaptiveRestBreakScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive rest break score. */
data class AdaptiveRestBreakScoreValidator(var rules: MutableList<AdaptiveRestBreakScoreRule>) {
    fun validate(item: AdaptiveRestBreakScore): MutableList<String> {
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

    fun requireValid(item: AdaptiveRestBreakScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveRestBreakScore>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveRestBreakScoreValidator(): AdaptiveRestBreakScoreValidator {
    return AdaptiveRestBreakScoreValidator(
        mutableListOf<AdaptiveRestBreakScoreRule>(
            AdaptiveRestBreakScoreRequiredLaneRule("adaptive rest break score needs this field"),
            AdaptiveRestBreakScoreAttemptsRangeRule(120, 86400),
            AdaptiveRestBreakScoreDescriptionLengthRule(86400),
            AdaptiveRestBreakScoreCodeLengthRule(64),
        ),
    )
}
