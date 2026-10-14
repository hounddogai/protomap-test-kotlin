package com.acme.transport.safety.scorecard.allocation

/** A rule a adaptive scorecard checkpoint must pass. Check returns its problems, or none. */
interface AdaptiveScorecardCheckpointRule {
    fun check(item: AdaptiveScorecardCheckpoint): MutableList<String>
}

data class AdaptiveScorecardCheckpointActiveGuardRule(var limit: Double) : AdaptiveScorecardCheckpointRule {
    override fun check(item: AdaptiveScorecardCheckpoint): MutableList<String> {
        if (item.active && item.ratio > limit) {
            return mutableListOf<String>("ratio ${item.ratio} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveScorecardCheckpointTitleLengthRule(var maxLength: Int) : AdaptiveScorecardCheckpointRule {
    override fun check(item: AdaptiveScorecardCheckpoint): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveScorecardCheckpointCategoryLengthRule(var maxLength: Int) : AdaptiveScorecardCheckpointRule {
    override fun check(item: AdaptiveScorecardCheckpoint): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveScorecardCheckpointCeilingRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveScorecardCheckpointRule {
    override fun check(item: AdaptiveScorecardCheckpoint): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive scorecard checkpoint. */
data class AdaptiveScorecardCheckpointValidator(var rules: MutableList<AdaptiveScorecardCheckpointRule>) {
    fun validate(item: AdaptiveScorecardCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveScorecardCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveScorecardCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveScorecardCheckpointValidator(): AdaptiveScorecardCheckpointValidator {
    return AdaptiveScorecardCheckpointValidator(
        mutableListOf<AdaptiveScorecardCheckpointRule>(
            AdaptiveScorecardCheckpointActiveGuardRule(6.9),
            AdaptiveScorecardCheckpointTitleLengthRule(86400),
            AdaptiveScorecardCheckpointCategoryLengthRule(16),
            AdaptiveScorecardCheckpointCeilingRangeRule(7.7, 3796.1),
        ),
    )
}
