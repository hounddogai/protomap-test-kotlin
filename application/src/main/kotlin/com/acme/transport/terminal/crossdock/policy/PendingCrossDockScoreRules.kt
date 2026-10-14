package com.acme.transport.terminal.crossdock.policy

/** A rule a pending cross dock score must pass. Check returns its problems, or none. */
interface PendingCrossDockScoreRule {
    fun check(item: PendingCrossDockScore): MutableList<String>
}

data class PendingCrossDockScoreRequiredTitleRule(var message: String) : PendingCrossDockScoreRule {
    override fun check(item: PendingCrossDockScore): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingCrossDockScoreCodeLengthRule(var maxLength: Int) : PendingCrossDockScoreRule {
    override fun check(item: PendingCrossDockScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingCrossDockScoreUpdatedAtRangeRule(var min: Int, var max: Int) : PendingCrossDockScoreRule {
    override fun check(item: PendingCrossDockScore): MutableList<String> {
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

/** Runs every rule of a pending cross dock score. */
data class PendingCrossDockScoreValidator(var rules: MutableList<PendingCrossDockScoreRule>) {
    fun validate(item: PendingCrossDockScore): MutableList<String> {
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

    fun requireValid(item: PendingCrossDockScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCrossDockScore>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCrossDockScoreValidator(): PendingCrossDockScoreValidator {
    return PendingCrossDockScoreValidator(
        mutableListOf<PendingCrossDockScoreRule>(
            PendingCrossDockScoreRequiredTitleRule("pending cross dock score needs this field"),
            PendingCrossDockScoreCodeLengthRule(64),
            PendingCrossDockScoreUpdatedAtRangeRule(16, 3600),
        ),
    )
}
