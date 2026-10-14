package com.acme.transport.lastmile.attempt.attemptdraft

/** A rule a default attempt checkpoint must pass. Check returns its problems, or none. */
interface DefaultAttemptCheckpointRule {
    fun check(item: DefaultAttemptCheckpoint): MutableList<String>
}

data class DefaultAttemptCheckpointRequiredLabelRule(var message: String) : DefaultAttemptCheckpointRule {
    override fun check(item: DefaultAttemptCheckpoint): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultAttemptCheckpointCodePrefixRule(var prefix: String) : DefaultAttemptCheckpointRule {
    override fun check(item: DefaultAttemptCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAttemptCheckpointBudgetRangeRule(
    var min: Double,
    var max: Double,
) : DefaultAttemptCheckpointRule {
    override fun check(item: DefaultAttemptCheckpoint): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default attempt checkpoint. */
data class DefaultAttemptCheckpointValidator(var rules: MutableList<DefaultAttemptCheckpointRule>) {
    fun validate(item: DefaultAttemptCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultAttemptCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultAttemptCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultAttemptCheckpointValidator(): DefaultAttemptCheckpointValidator {
    return DefaultAttemptCheckpointValidator(
        mutableListOf<DefaultAttemptCheckpointRule>(
            DefaultAttemptCheckpointRequiredLabelRule("default attempt checkpoint needs an entry"),
            DefaultAttemptCheckpointCodePrefixRule("ACM"),
            DefaultAttemptCheckpointBudgetRangeRule(5.5, 3036.8),
        ),
    )
}
