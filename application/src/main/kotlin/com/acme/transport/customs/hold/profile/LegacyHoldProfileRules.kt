package com.acme.transport.customs.hold.profile

/** A rule a legacy hold profile must pass. Check returns its problems, or none. */
interface LegacyHoldProfileRule {
    fun check(item: LegacyHoldProfile): MutableList<String>
}

data class LegacyHoldProfileRequiredNameRule(var message: String) : LegacyHoldProfileRule {
    override fun check(item: LegacyHoldProfile): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyHoldProfileCodePrefixRule(var prefix: String) : LegacyHoldProfileRule {
    override fun check(item: LegacyHoldProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyHoldProfileTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : LegacyHoldProfileRule {
    override fun check(item: LegacyHoldProfile): MutableList<String> {
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

/** Runs every rule of a legacy hold profile. */
data class LegacyHoldProfileValidator(var rules: MutableList<LegacyHoldProfileRule>) {
    fun validate(item: LegacyHoldProfile): MutableList<String> {
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

    fun requireValid(item: LegacyHoldProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyHoldProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyHoldProfileValidator(): LegacyHoldProfileValidator {
    return LegacyHoldProfileValidator(
        mutableListOf<LegacyHoldProfileRule>(
            LegacyHoldProfileRequiredNameRule("legacy hold profile needs a value"),
            LegacyHoldProfileCodePrefixRule("Q"),
            LegacyHoldProfileTagsLimitRule(32, mutableListOf<String>("weighted", "capped")),
        ),
    )
}
