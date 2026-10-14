package com.acme.transport.lastmile.attempt.template

/** A rule a attempt cycle must pass. Check returns its problems, or none. */
interface AttemptCycleRule {
    fun check(item: AttemptCycle): MutableList<String>
}

data class AttemptCycleRequiredNotesRule(var message: String) : AttemptCycleRule {
    override fun check(item: AttemptCycle): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptCycleRequiredDepotRule(var message: String) : AttemptCycleRule {
    override fun check(item: AttemptCycle): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptCycleDepotLengthRule(var maxLength: Int) : AttemptCycleRule {
    override fun check(item: AttemptCycle): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt cycle. */
data class AttemptCycleValidator(var rules: MutableList<AttemptCycleRule>) {
    fun validate(item: AttemptCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AttemptCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptCycleValidator(): AttemptCycleValidator {
    return AttemptCycleValidator(
        mutableListOf<AttemptCycleRule>(
            AttemptCycleRequiredNotesRule("attempt cycle needs this field"),
            AttemptCycleRequiredDepotRule("attempt cycle needs this field"),
            AttemptCycleDepotLengthRule(64),
        ),
    )
}
