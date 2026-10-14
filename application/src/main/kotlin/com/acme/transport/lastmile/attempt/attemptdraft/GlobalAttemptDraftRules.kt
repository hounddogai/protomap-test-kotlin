package com.acme.transport.lastmile.attempt.attemptdraft

/** A rule a global attempt draft must pass. Check returns its problems, or none. */
interface GlobalAttemptDraftRule {
    fun check(item: GlobalAttemptDraft): MutableList<String>
}

data class GlobalAttemptDraftUpdatedAtRangeRule(var min: Int, var max: Int) : GlobalAttemptDraftRule {
    override fun check(item: GlobalAttemptDraft): MutableList<String> {
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

data class GlobalAttemptDraftChecksumLengthRule(var maxLength: Int) : GlobalAttemptDraftRule {
    override fun check(item: GlobalAttemptDraft): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAttemptDraftCodePrefixRule(var prefix: String) : GlobalAttemptDraftRule {
    override fun check(item: GlobalAttemptDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAttemptDraftRequiredTitleRule(var message: String) : GlobalAttemptDraftRule {
    override fun check(item: GlobalAttemptDraft): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global attempt draft. */
data class GlobalAttemptDraftValidator(var rules: MutableList<GlobalAttemptDraftRule>) {
    fun validate(item: GlobalAttemptDraft): MutableList<String> {
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

    fun requireValid(item: GlobalAttemptDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalAttemptDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalAttemptDraftValidator(): GlobalAttemptDraftValidator {
    return GlobalAttemptDraftValidator(
        mutableListOf<GlobalAttemptDraftRule>(
            GlobalAttemptDraftUpdatedAtRangeRule(16, 3600),
            GlobalAttemptDraftChecksumLengthRule(255),
            GlobalAttemptDraftCodePrefixRule("ACM"),
            GlobalAttemptDraftRequiredTitleRule("global attempt draft needs an entry"),
        ),
    )
}
