package com.acme.transport.terminal.traileryard.recorditem

/** A rule a default trailer yard checkpoint must pass. Check returns its problems, or none. */
interface DefaultTrailerYardCheckpointRule {
    fun check(item: DefaultTrailerYardCheckpoint): MutableList<String>
}

data class DefaultTrailerYardCheckpointCodeLengthRule(var maxLength: Int) : DefaultTrailerYardCheckpointRule {
    override fun check(item: DefaultTrailerYardCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTrailerYardCheckpointRequiredDockRule(
    var message: String,
) : DefaultTrailerYardCheckpointRule {
    override fun check(item: DefaultTrailerYardCheckpoint): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultTrailerYardCheckpointRequiredReviewerRule(
    var message: String,
) : DefaultTrailerYardCheckpointRule {
    override fun check(item: DefaultTrailerYardCheckpoint): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default trailer yard checkpoint. */
data class DefaultTrailerYardCheckpointValidator(var rules: MutableList<DefaultTrailerYardCheckpointRule>) {
    fun validate(item: DefaultTrailerYardCheckpoint): MutableList<String> {
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

    fun requireValid(item: DefaultTrailerYardCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTrailerYardCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTrailerYardCheckpointValidator(): DefaultTrailerYardCheckpointValidator {
    return DefaultTrailerYardCheckpointValidator(
        mutableListOf<DefaultTrailerYardCheckpointRule>(
            DefaultTrailerYardCheckpointCodeLengthRule(1000),
            DefaultTrailerYardCheckpointRequiredDockRule("default trailer yard checkpoint needs an entry"),
            DefaultTrailerYardCheckpointRequiredReviewerRule("default trailer yard checkpoint needs an entry"),
        ),
    )
}
