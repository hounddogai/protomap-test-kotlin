package com.acme.transport.lastmile.attempt.batch

/** A rule a rolling attempt threshold must pass. Check returns its problems, or none. */
interface RollingAttemptThresholdRule {
    fun check(item: RollingAttemptThreshold): MutableList<String>
}

data class RollingAttemptThresholdNotesLengthRule(var maxLength: Int) : RollingAttemptThresholdRule {
    override fun check(item: RollingAttemptThreshold): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingAttemptThresholdRequiredNotesRule(var message: String) : RollingAttemptThresholdRule {
    override fun check(item: RollingAttemptThreshold): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingAttemptThresholdCarrierLengthRule(var maxLength: Int) : RollingAttemptThresholdRule {
    override fun check(item: RollingAttemptThreshold): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingAttemptThresholdCodePrefixRule(var prefix: String) : RollingAttemptThresholdRule {
    override fun check(item: RollingAttemptThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingAttemptThresholdRequiredCodeRule(var message: String) : RollingAttemptThresholdRule {
    override fun check(item: RollingAttemptThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling attempt threshold. */
data class RollingAttemptThresholdValidator(var rules: MutableList<RollingAttemptThresholdRule>) {
    fun validate(item: RollingAttemptThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingAttemptThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingAttemptThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingAttemptThresholdValidator(): RollingAttemptThresholdValidator {
    return RollingAttemptThresholdValidator(
        mutableListOf<RollingAttemptThresholdRule>(
            RollingAttemptThresholdNotesLengthRule(3600),
            RollingAttemptThresholdRequiredNotesRule("rolling attempt threshold needs this field"),
            RollingAttemptThresholdCarrierLengthRule(86400),
            RollingAttemptThresholdCodePrefixRule("Q"),
            RollingAttemptThresholdRequiredCodeRule("rolling attempt threshold needs a value"),
        ),
    )
}
