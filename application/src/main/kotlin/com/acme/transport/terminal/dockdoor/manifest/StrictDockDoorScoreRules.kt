package com.acme.transport.terminal.dockdoor.manifest

/** A rule a strict dock door score must pass. Check returns its problems, or none. */
interface StrictDockDoorScoreRule {
    fun check(item: StrictDockDoorScore): MutableList<String>
}

data class StrictDockDoorScoreAttemptsRangeRule(var min: Int, var max: Int) : StrictDockDoorScoreRule {
    override fun check(item: StrictDockDoorScore): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class StrictDockDoorScoreCodePrefixRule(var prefix: String) : StrictDockDoorScoreRule {
    override fun check(item: StrictDockDoorScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictDockDoorScoreCodeLengthRule(var maxLength: Int) : StrictDockDoorScoreRule {
    override fun check(item: StrictDockDoorScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict dock door score. */
data class StrictDockDoorScoreValidator(var rules: MutableList<StrictDockDoorScoreRule>) {
    fun validate(item: StrictDockDoorScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictDockDoorScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDockDoorScore>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDockDoorScoreValidator(): StrictDockDoorScoreValidator {
    return StrictDockDoorScoreValidator(
        mutableListOf<StrictDockDoorScoreRule>(
            StrictDockDoorScoreAttemptsRangeRule(255, 86400),
            StrictDockDoorScoreCodePrefixRule("Q"),
            StrictDockDoorScoreCodeLengthRule(16),
        ),
    )
}
