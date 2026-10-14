package com.acme.transport.lastmile.attempt.attemptpolicy

/** A rule a fallback attempt policy must pass. Check returns its problems, or none. */
interface FallbackAttemptPolicyRule {
    fun check(item: FallbackAttemptPolicy): MutableList<String>
}

data class FallbackAttemptPolicyTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : FallbackAttemptPolicyRule {
    override fun check(item: FallbackAttemptPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class FallbackAttemptPolicyRequiredNameRule(var message: String) : FallbackAttemptPolicyRule {
    override fun check(item: FallbackAttemptPolicy): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackAttemptPolicyRequiredLocaleRule(var message: String) : FallbackAttemptPolicyRule {
    override fun check(item: FallbackAttemptPolicy): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackAttemptPolicyCodeLengthRule(var maxLength: Int) : FallbackAttemptPolicyRule {
    override fun check(item: FallbackAttemptPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAttemptPolicyNameLengthRule(var maxLength: Int) : FallbackAttemptPolicyRule {
    override fun check(item: FallbackAttemptPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback attempt policy. */
data class FallbackAttemptPolicyValidator(var rules: MutableList<FallbackAttemptPolicyRule>) {
    fun validate(item: FallbackAttemptPolicy): MutableList<String> {
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

    fun requireValid(item: FallbackAttemptPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackAttemptPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackAttemptPolicyValidator(): FallbackAttemptPolicyValidator {
    return FallbackAttemptPolicyValidator(
        mutableListOf<FallbackAttemptPolicyRule>(
            FallbackAttemptPolicyTagsLimitRule(120, mutableListOf<String>()),
            FallbackAttemptPolicyRequiredNameRule("fallback attempt policy needs a value"),
            FallbackAttemptPolicyRequiredLocaleRule("fallback attempt policy needs an entry"),
            FallbackAttemptPolicyCodeLengthRule(1000),
            FallbackAttemptPolicyNameLengthRule(32),
        ),
    )
}
