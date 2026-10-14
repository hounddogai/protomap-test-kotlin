package com.acme.transport.safety.scorecard.scorecardgroup

/** A rule a scorecard checkpoint must pass. Check returns its problems, or none. */
interface ScorecardCheckpointRule {
    fun check(item: ScorecardCheckpoint): MutableList<String>
}

data class ScorecardCheckpointPalletsRangeRule(var min: Int, var max: Int) : ScorecardCheckpointRule {
    override fun check(item: ScorecardCheckpoint): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardCheckpointRequiredNameRule(var message: String) : ScorecardCheckpointRule {
    override fun check(item: ScorecardCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardCheckpointRequiredCodeRule(var message: String) : ScorecardCheckpointRule {
    override fun check(item: ScorecardCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardCheckpointNotesLengthRule(var maxLength: Int) : ScorecardCheckpointRule {
    override fun check(item: ScorecardCheckpoint): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardCheckpointCodeLengthRule(var maxLength: Int) : ScorecardCheckpointRule {
    override fun check(item: ScorecardCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardCheckpointNameLengthRule(var maxLength: Int) : ScorecardCheckpointRule {
    override fun check(item: ScorecardCheckpoint): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard checkpoint. */
data class ScorecardCheckpointValidator(var rules: MutableList<ScorecardCheckpointRule>) {
    fun validate(item: ScorecardCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScorecardCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardCheckpointValidator(): ScorecardCheckpointValidator {
    return ScorecardCheckpointValidator(
        mutableListOf<ScorecardCheckpointRule>(
            ScorecardCheckpointPalletsRangeRule(16, 64),
            ScorecardCheckpointRequiredNameRule("scorecard checkpoint needs a value"),
            ScorecardCheckpointRequiredCodeRule("scorecard checkpoint needs a value"),
            ScorecardCheckpointNotesLengthRule(1000),
            ScorecardCheckpointCodeLengthRule(32),
            ScorecardCheckpointNameLengthRule(255),
        ),
    )
}
