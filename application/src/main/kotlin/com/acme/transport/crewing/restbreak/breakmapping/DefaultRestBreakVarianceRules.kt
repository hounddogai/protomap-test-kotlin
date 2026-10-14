package com.acme.transport.crewing.restbreak.breakmapping

/** A rule a default rest break variance must pass. Check returns its problems, or none. */
interface DefaultRestBreakVarianceRule {
    fun check(item: DefaultRestBreakVariance): MutableList<String>
}

data class DefaultRestBreakVarianceCodeLengthRule(var maxLength: Int) : DefaultRestBreakVarianceRule {
    override fun check(item: DefaultRestBreakVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRestBreakVarianceTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : DefaultRestBreakVarianceRule {
    override fun check(item: DefaultRestBreakVariance): MutableList<String> {
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

data class DefaultRestBreakVarianceNotesLengthRule(var maxLength: Int) : DefaultRestBreakVarianceRule {
    override fun check(item: DefaultRestBreakVariance): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default rest break variance. */
data class DefaultRestBreakVarianceValidator(var rules: MutableList<DefaultRestBreakVarianceRule>) {
    fun validate(item: DefaultRestBreakVariance): MutableList<String> {
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

    fun requireValid(item: DefaultRestBreakVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRestBreakVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRestBreakVarianceValidator(): DefaultRestBreakVarianceValidator {
    return DefaultRestBreakVarianceValidator(
        mutableListOf<DefaultRestBreakVarianceRule>(
            DefaultRestBreakVarianceCodeLengthRule(1000),
            DefaultRestBreakVarianceTagsLimitRule(16, mutableListOf<String>("legacy", "strict")),
            DefaultRestBreakVarianceNotesLengthRule(255),
        ),
    )
}
