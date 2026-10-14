package com.acme.transport.crewing.roster.score

/** A rule a adaptive roster score must pass. Check returns its problems, or none. */
interface AdaptiveRosterScoreRule {
    fun check(item: AdaptiveRosterScore): MutableList<String>
}

data class AdaptiveRosterScoreBatchSizeRangeRule(var min: Int, var max: Int) : AdaptiveRosterScoreRule {
    override fun check(item: AdaptiveRosterScore): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class AdaptiveRosterScoreRequiredCodeRule(var message: String) : AdaptiveRosterScoreRule {
    override fun check(item: AdaptiveRosterScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRosterScoreTitleLengthRule(var maxLength: Int) : AdaptiveRosterScoreRule {
    override fun check(item: AdaptiveRosterScore): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRosterScoreCodeLengthRule(var maxLength: Int) : AdaptiveRosterScoreRule {
    override fun check(item: AdaptiveRosterScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive roster score. */
data class AdaptiveRosterScoreValidator(var rules: MutableList<AdaptiveRosterScoreRule>) {
    fun validate(item: AdaptiveRosterScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveRosterScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveRosterScore>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveRosterScoreValidator(): AdaptiveRosterScoreValidator {
    return AdaptiveRosterScoreValidator(
        mutableListOf<AdaptiveRosterScoreRule>(
            AdaptiveRosterScoreBatchSizeRangeRule(32, 3600),
            AdaptiveRosterScoreRequiredCodeRule("adaptive roster score needs a value"),
            AdaptiveRosterScoreTitleLengthRule(255),
            AdaptiveRosterScoreCodeLengthRule(80),
        ),
    )
}
