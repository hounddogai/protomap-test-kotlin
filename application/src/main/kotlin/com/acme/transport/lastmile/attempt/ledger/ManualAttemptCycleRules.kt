package com.acme.transport.lastmile.attempt.ledger

/** A rule a manual attempt cycle must pass. Check returns its problems, or none. */
interface ManualAttemptCycleRule {
    fun check(item: ManualAttemptCycle): MutableList<String>
}

data class ManualAttemptCycleSequenceRangeRule(var min: Int, var max: Int) : ManualAttemptCycleRule {
    override fun check(item: ManualAttemptCycle): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualAttemptCyclePalletsRangeRule(var min: Int, var max: Int) : ManualAttemptCycleRule {
    override fun check(item: ManualAttemptCycle): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualAttemptCycleUpdatedAtRangeRule(var min: Int, var max: Int) : ManualAttemptCycleRule {
    override fun check(item: ManualAttemptCycle): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class ManualAttemptCycleScoreRangeRule(var min: Double, var max: Double) : ManualAttemptCycleRule {
    override fun check(item: ManualAttemptCycle): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class ManualAttemptCycleRequiredCodeRule(var message: String) : ManualAttemptCycleRule {
    override fun check(item: ManualAttemptCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual attempt cycle. */
data class ManualAttemptCycleValidator(var rules: MutableList<ManualAttemptCycleRule>) {
    fun validate(item: ManualAttemptCycle): MutableList<String> {
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

    fun requireValid(item: ManualAttemptCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualAttemptCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultManualAttemptCycleValidator(): ManualAttemptCycleValidator {
    return ManualAttemptCycleValidator(
        mutableListOf<ManualAttemptCycleRule>(
            ManualAttemptCycleSequenceRangeRule(16, 1000),
            ManualAttemptCyclePalletsRangeRule(120, 3600),
            ManualAttemptCycleUpdatedAtRangeRule(255, 3600),
            ManualAttemptCycleScoreRangeRule(6.8, 1788.5),
            ManualAttemptCycleRequiredCodeRule("manual attempt cycle needs an entry"),
        ),
    )
}
