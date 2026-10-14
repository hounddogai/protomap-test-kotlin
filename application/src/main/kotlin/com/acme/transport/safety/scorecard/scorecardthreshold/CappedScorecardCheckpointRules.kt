package com.acme.transport.safety.scorecard.scorecardthreshold

/** A rule a capped scorecard checkpoint must pass. Check returns its problems, or none. */
interface CappedScorecardCheckpointRule {
    fun check(item: CappedScorecardCheckpoint): MutableList<String>
}

data class CappedScorecardCheckpointCodeLengthRule(var maxLength: Int) : CappedScorecardCheckpointRule {
    override fun check(item: CappedScorecardCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedScorecardCheckpointBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : CappedScorecardCheckpointRule {
    override fun check(item: CappedScorecardCheckpoint): MutableList<String> {
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

data class CappedScorecardCheckpointLabelLengthRule(var maxLength: Int) : CappedScorecardCheckpointRule {
    override fun check(item: CappedScorecardCheckpoint): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedScorecardCheckpointCeilingRangeRule(
    var min: Double,
    var max: Double,
) : CappedScorecardCheckpointRule {
    override fun check(item: CappedScorecardCheckpoint): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped scorecard checkpoint. */
data class CappedScorecardCheckpointValidator(var rules: MutableList<CappedScorecardCheckpointRule>) {
    fun validate(item: CappedScorecardCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedScorecardCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedScorecardCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedScorecardCheckpointValidator(): CappedScorecardCheckpointValidator {
    return CappedScorecardCheckpointValidator(
        mutableListOf<CappedScorecardCheckpointRule>(
            CappedScorecardCheckpointCodeLengthRule(3600),
            CappedScorecardCheckpointBatchSizeRangeRule(1000, 1000),
            CappedScorecardCheckpointLabelLengthRule(1000),
            CappedScorecardCheckpointCeilingRangeRule(4.2, 3789.5),
        ),
    )
}
