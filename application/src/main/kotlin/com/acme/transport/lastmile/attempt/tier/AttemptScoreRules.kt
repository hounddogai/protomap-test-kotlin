package com.acme.transport.lastmile.attempt.tier

/** A rule a attempt score must pass. Check returns its problems, or none. */
interface AttemptScoreRule {
    fun check(item: AttemptScore): MutableList<String>
}

data class AttemptScorePriorityRangeRule(var min: Int, var max: Int) : AttemptScoreRule {
    override fun check(item: AttemptScore): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class AttemptScoreStopCountRangeRule(var min: Int, var max: Int) : AttemptScoreRule {
    override fun check(item: AttemptScore): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class AttemptScoreRequiredNameRule(var message: String) : AttemptScoreRule {
    override fun check(item: AttemptScore): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptScoreLaneLengthRule(var maxLength: Int) : AttemptScoreRule {
    override fun check(item: AttemptScore): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt score. */
data class AttemptScoreValidator(var rules: MutableList<AttemptScoreRule>) {
    fun validate(item: AttemptScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AttemptScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptScore>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptScoreValidator(): AttemptScoreValidator {
    return AttemptScoreValidator(
        mutableListOf<AttemptScoreRule>(
            AttemptScorePriorityRangeRule(3600, 86400),
            AttemptScoreStopCountRangeRule(80, 1000),
            AttemptScoreRequiredNameRule("attempt score needs a value"),
            AttemptScoreLaneLengthRule(120),
        ),
    )
}
