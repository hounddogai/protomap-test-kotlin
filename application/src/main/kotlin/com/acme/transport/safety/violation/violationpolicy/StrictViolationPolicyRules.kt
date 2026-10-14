package com.acme.transport.safety.violation.violationpolicy

/** A rule a strict violation policy must pass. Check returns its problems, or none. */
interface StrictViolationPolicyRule {
    fun check(item: StrictViolationPolicy): MutableList<String>
}

data class StrictViolationPolicyRequiredOwnerIdRule(var message: String) : StrictViolationPolicyRule {
    override fun check(item: StrictViolationPolicy): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictViolationPolicyRequiredNameRule(var message: String) : StrictViolationPolicyRule {
    override fun check(item: StrictViolationPolicy): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictViolationPolicyCodePrefixRule(var prefix: String) : StrictViolationPolicyRule {
    override fun check(item: StrictViolationPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictViolationPolicyRequiredCodeRule(var message: String) : StrictViolationPolicyRule {
    override fun check(item: StrictViolationPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictViolationPolicyTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : StrictViolationPolicyRule {
    override fun check(item: StrictViolationPolicy): MutableList<String> {
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

data class StrictViolationPolicyNameLengthRule(var maxLength: Int) : StrictViolationPolicyRule {
    override fun check(item: StrictViolationPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict violation policy. */
data class StrictViolationPolicyValidator(var rules: MutableList<StrictViolationPolicyRule>) {
    fun validate(item: StrictViolationPolicy): MutableList<String> {
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

    fun requireValid(item: StrictViolationPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictViolationPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictViolationPolicyValidator(): StrictViolationPolicyValidator {
    return StrictViolationPolicyValidator(
        mutableListOf<StrictViolationPolicyRule>(
            StrictViolationPolicyRequiredOwnerIdRule("strict violation policy needs this field"),
            StrictViolationPolicyRequiredNameRule("strict violation policy needs an entry"),
            StrictViolationPolicyCodePrefixRule("X"),
            StrictViolationPolicyRequiredCodeRule("strict violation policy needs this field"),
            StrictViolationPolicyTagsLimitRule(80, mutableListOf<String>()),
            StrictViolationPolicyNameLengthRule(255),
        ),
    )
}
