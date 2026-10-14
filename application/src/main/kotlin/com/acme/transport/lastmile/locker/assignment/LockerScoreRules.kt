package com.acme.transport.lastmile.locker.assignment

/** A rule a locker score must pass. Check returns its problems, or none. */
interface LockerScoreRule {
    fun check(item: LockerScore): MutableList<String>
}

data class LockerScoreStartsAtRangeRule(var min: Int, var max: Int) : LockerScoreRule {
    override fun check(item: LockerScore): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class LockerScoreCodePrefixRule(var prefix: String) : LockerScoreRule {
    override fun check(item: LockerScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LockerScoreRequiredNotesRule(var message: String) : LockerScoreRule {
    override fun check(item: LockerScore): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker score. */
data class LockerScoreValidator(var rules: MutableList<LockerScoreRule>) {
    fun validate(item: LockerScore): MutableList<String> {
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

    fun requireValid(item: LockerScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerScore>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerScoreValidator(): LockerScoreValidator {
    return LockerScoreValidator(
        mutableListOf<LockerScoreRule>(
            LockerScoreStartsAtRangeRule(64, 1000),
            LockerScoreCodePrefixRule("Q"),
            LockerScoreRequiredNotesRule("locker score needs a value"),
        ),
    )
}
