package com.acme.transport.crewing.swap.draft

/** A rule a rolling swap draft must pass. Check returns its problems, or none. */
interface RollingSwapDraftRule {
    fun check(item: RollingSwapDraft): MutableList<String>
}

data class RollingSwapDraftRequiredNameRule(var message: String) : RollingSwapDraftRule {
    override fun check(item: RollingSwapDraft): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingSwapDraftCodeLengthRule(var maxLength: Int) : RollingSwapDraftRule {
    override fun check(item: RollingSwapDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingSwapDraftCodePrefixRule(var prefix: String) : RollingSwapDraftRule {
    override fun check(item: RollingSwapDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingSwapDraftRetriesRangeRule(var min: Int, var max: Int) : RollingSwapDraftRule {
    override fun check(item: RollingSwapDraft): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rolling swap draft. */
data class RollingSwapDraftValidator(var rules: MutableList<RollingSwapDraftRule>) {
    fun validate(item: RollingSwapDraft): MutableList<String> {
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

    fun requireValid(item: RollingSwapDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingSwapDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingSwapDraftValidator(): RollingSwapDraftValidator {
    return RollingSwapDraftValidator(
        mutableListOf<RollingSwapDraftRule>(
            RollingSwapDraftRequiredNameRule("rolling swap draft needs an entry"),
            RollingSwapDraftCodeLengthRule(64),
            RollingSwapDraftCodePrefixRule("Q"),
            RollingSwapDraftRetriesRangeRule(32, 32),
        ),
    )
}
