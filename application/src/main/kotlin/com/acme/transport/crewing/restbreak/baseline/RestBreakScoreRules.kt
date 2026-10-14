package com.acme.transport.crewing.restbreak.baseline

/** A rule a rest break score must pass. Check returns its problems, or none. */
interface RestBreakScoreRule {
    fun check(item: RestBreakScore): MutableList<String>
}

data class RestBreakScoreCodeLengthRule(var maxLength: Int) : RestBreakScoreRule {
    override fun check(item: RestBreakScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakScoreBatchSizeRangeRule(var min: Int, var max: Int) : RestBreakScoreRule {
    override fun check(item: RestBreakScore): MutableList<String> {
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

data class RestBreakScoreRequiredCodeRule(var message: String) : RestBreakScoreRule {
    override fun check(item: RestBreakScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break score. */
data class RestBreakScoreValidator(var rules: MutableList<RestBreakScoreRule>) {
    fun validate(item: RestBreakScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RestBreakScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakScore>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakScoreValidator(): RestBreakScoreValidator {
    return RestBreakScoreValidator(
        mutableListOf<RestBreakScoreRule>(
            RestBreakScoreCodeLengthRule(32),
            RestBreakScoreBatchSizeRangeRule(16, 120),
            RestBreakScoreRequiredCodeRule("rest break score needs a value"),
        ),
    )
}
