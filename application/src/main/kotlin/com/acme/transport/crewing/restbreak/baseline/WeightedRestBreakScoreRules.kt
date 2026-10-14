package com.acme.transport.crewing.restbreak.baseline

/** A rule a weighted rest break score must pass. Check returns its problems, or none. */
interface WeightedRestBreakScoreRule {
    fun check(item: WeightedRestBreakScore): MutableList<String>
}

data class WeightedRestBreakScoreTitleLengthRule(var maxLength: Int) : WeightedRestBreakScoreRule {
    override fun check(item: WeightedRestBreakScore): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRestBreakScoreRequiredCodeRule(var message: String) : WeightedRestBreakScoreRule {
    override fun check(item: WeightedRestBreakScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedRestBreakScoreApprovedGuardRule(var limit: Int) : WeightedRestBreakScoreRule {
    override fun check(item: WeightedRestBreakScore): MutableList<String> {
        if (item.approved && item.updatedAt > limit) {
            return mutableListOf<String>("updated at ${item.updatedAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted rest break score. */
data class WeightedRestBreakScoreValidator(var rules: MutableList<WeightedRestBreakScoreRule>) {
    fun validate(item: WeightedRestBreakScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedRestBreakScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedRestBreakScore>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedRestBreakScoreValidator(): WeightedRestBreakScoreValidator {
    return WeightedRestBreakScoreValidator(
        mutableListOf<WeightedRestBreakScoreRule>(
            WeightedRestBreakScoreTitleLengthRule(32),
            WeightedRestBreakScoreRequiredCodeRule("weighted rest break score needs a value"),
            WeightedRestBreakScoreApprovedGuardRule(255),
        ),
    )
}
