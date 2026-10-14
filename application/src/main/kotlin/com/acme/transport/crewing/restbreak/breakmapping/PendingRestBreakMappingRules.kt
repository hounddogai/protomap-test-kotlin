package com.acme.transport.crewing.restbreak.breakmapping

/** A rule a pending rest break mapping must pass. Check returns its problems, or none. */
interface PendingRestBreakMappingRule {
    fun check(item: PendingRestBreakMapping): MutableList<String>
}

data class PendingRestBreakMappingTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : PendingRestBreakMappingRule {
    override fun check(item: PendingRestBreakMapping): MutableList<String> {
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

data class PendingRestBreakMappingCodePrefixRule(var prefix: String) : PendingRestBreakMappingRule {
    override fun check(item: PendingRestBreakMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingRestBreakMappingRequiredTitleRule(var message: String) : PendingRestBreakMappingRule {
    override fun check(item: PendingRestBreakMapping): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRestBreakMappingRequiredExternalRefRule(var message: String) : PendingRestBreakMappingRule {
    override fun check(item: PendingRestBreakMapping): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRestBreakMappingExternalRefLengthRule(var maxLength: Int) : PendingRestBreakMappingRule {
    override fun check(item: PendingRestBreakMapping): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending rest break mapping. */
data class PendingRestBreakMappingValidator(var rules: MutableList<PendingRestBreakMappingRule>) {
    fun validate(item: PendingRestBreakMapping): MutableList<String> {
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

    fun requireValid(item: PendingRestBreakMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingRestBreakMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingRestBreakMappingValidator(): PendingRestBreakMappingValidator {
    return PendingRestBreakMappingValidator(
        mutableListOf<PendingRestBreakMappingRule>(
            PendingRestBreakMappingTagsLimitRule(1000, mutableListOf<String>()),
            PendingRestBreakMappingCodePrefixRule("ACM"),
            PendingRestBreakMappingRequiredTitleRule("pending rest break mapping needs an entry"),
            PendingRestBreakMappingRequiredExternalRefRule("pending rest break mapping needs this field"),
            PendingRestBreakMappingExternalRefLengthRule(86400),
        ),
    )
}
