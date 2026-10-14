package com.acme.transport.lastmile.attempt.window

/** A rule a pending attempt request must pass. Check returns its problems, or none. */
interface PendingAttemptRequestRule {
    fun check(item: PendingAttemptRequest): MutableList<String>
}

data class PendingAttemptRequestRequiredLabelRule(var message: String) : PendingAttemptRequestRule {
    override fun check(item: PendingAttemptRequest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAttemptRequestTrailerIdLengthRule(var maxLength: Int) : PendingAttemptRequestRule {
    override fun check(item: PendingAttemptRequest): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingAttemptRequestRequiredTrailerIdRule(var message: String) : PendingAttemptRequestRule {
    override fun check(item: PendingAttemptRequest): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAttemptRequestCodePrefixRule(var prefix: String) : PendingAttemptRequestRule {
    override fun check(item: PendingAttemptRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending attempt request. */
data class PendingAttemptRequestValidator(var rules: MutableList<PendingAttemptRequestRule>) {
    fun validate(item: PendingAttemptRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingAttemptRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingAttemptRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingAttemptRequestValidator(): PendingAttemptRequestValidator {
    return PendingAttemptRequestValidator(
        mutableListOf<PendingAttemptRequestRule>(
            PendingAttemptRequestRequiredLabelRule("pending attempt request needs this field"),
            PendingAttemptRequestTrailerIdLengthRule(16),
            PendingAttemptRequestRequiredTrailerIdRule("pending attempt request needs an entry"),
            PendingAttemptRequestCodePrefixRule("REF"),
        ),
    )
}
