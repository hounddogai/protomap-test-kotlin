package com.acme.transport.terminal.crossdock.dockestimate

/** A rule a rolling cross dock score must pass. Check returns its problems, or none. */
interface RollingCrossDockScoreRule {
    fun check(item: RollingCrossDockScore): MutableList<String>
}

data class RollingCrossDockScoreScoreRangeRule(var min: Double, var max: Double) : RollingCrossDockScoreRule {
    override fun check(item: RollingCrossDockScore): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingCrossDockScoreRequiredCodeRule(var message: String) : RollingCrossDockScoreRule {
    override fun check(item: RollingCrossDockScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingCrossDockScoreRequiredTitleRule(var message: String) : RollingCrossDockScoreRule {
    override fun check(item: RollingCrossDockScore): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingCrossDockScoreTitleLengthRule(var maxLength: Int) : RollingCrossDockScoreRule {
    override fun check(item: RollingCrossDockScore): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling cross dock score. */
data class RollingCrossDockScoreValidator(var rules: MutableList<RollingCrossDockScoreRule>) {
    fun validate(item: RollingCrossDockScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingCrossDockScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingCrossDockScore>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingCrossDockScoreValidator(): RollingCrossDockScoreValidator {
    return RollingCrossDockScoreValidator(
        mutableListOf<RollingCrossDockScoreRule>(
            RollingCrossDockScoreScoreRangeRule(5.1, 3182.8),
            RollingCrossDockScoreRequiredCodeRule("rolling cross dock score needs an entry"),
            RollingCrossDockScoreRequiredTitleRule("rolling cross dock score needs this field"),
            RollingCrossDockScoreTitleLengthRule(80),
        ),
    )
}
