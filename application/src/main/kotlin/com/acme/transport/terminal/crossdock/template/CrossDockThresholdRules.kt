package com.acme.transport.terminal.crossdock.template

/** A rule a cross dock threshold must pass. Check returns its problems, or none. */
interface CrossDockThresholdRule {
    fun check(item: CrossDockThreshold): MutableList<String>
}

data class CrossDockThresholdRequiredCodeRule(var message: String) : CrossDockThresholdRule {
    override fun check(item: CrossDockThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockThresholdArchivedGuardRule(var limit: Int) : CrossDockThresholdRule {
    override fun check(item: CrossDockThreshold): MutableList<String> {
        if (item.archived && item.expiresAt > limit) {
            return mutableListOf<String>("expires at ${item.expiresAt} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockThresholdRequiredLabelRule(var message: String) : CrossDockThresholdRule {
    override fun check(item: CrossDockThreshold): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockThresholdLabelLengthRule(var maxLength: Int) : CrossDockThresholdRule {
    override fun check(item: CrossDockThreshold): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock threshold. */
data class CrossDockThresholdValidator(var rules: MutableList<CrossDockThresholdRule>) {
    fun validate(item: CrossDockThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockThresholdValidator(): CrossDockThresholdValidator {
    return CrossDockThresholdValidator(
        mutableListOf<CrossDockThresholdRule>(
            CrossDockThresholdRequiredCodeRule("cross dock threshold needs this field"),
            CrossDockThresholdArchivedGuardRule(16),
            CrossDockThresholdRequiredLabelRule("cross dock threshold needs this field"),
            CrossDockThresholdLabelLengthRule(80),
        ),
    )
}
