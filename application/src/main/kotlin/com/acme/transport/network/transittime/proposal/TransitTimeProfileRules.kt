package com.acme.transport.network.transittime.proposal

/** A rule a transit time profile must pass. Check returns its problems, or none. */
interface TransitTimeProfileRule {
    fun check(item: TransitTimeProfile): MutableList<String>
}

data class TransitTimeProfileCodePrefixRule(var prefix: String) : TransitTimeProfileRule {
    override fun check(item: TransitTimeProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeProfileNameLengthRule(var maxLength: Int) : TransitTimeProfileRule {
    override fun check(item: TransitTimeProfile): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeProfileTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : TransitTimeProfileRule {
    override fun check(item: TransitTimeProfile): MutableList<String> {
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

data class TransitTimeProfileCodeLengthRule(var maxLength: Int) : TransitTimeProfileRule {
    override fun check(item: TransitTimeProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time profile. */
data class TransitTimeProfileValidator(var rules: MutableList<TransitTimeProfileRule>) {
    fun validate(item: TransitTimeProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TransitTimeProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeProfileValidator(): TransitTimeProfileValidator {
    return TransitTimeProfileValidator(
        mutableListOf<TransitTimeProfileRule>(
            TransitTimeProfileCodePrefixRule("ACM"),
            TransitTimeProfileNameLengthRule(64),
            TransitTimeProfileTagsLimitRule(32, mutableListOf<String>("batched")),
            TransitTimeProfileCodeLengthRule(80),
        ),
    )
}
