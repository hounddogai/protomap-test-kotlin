package com.acme.transport.linehaul.tender.batch

/** A rule a tender override must pass. Check returns its problems, or none. */
interface TenderOverrideRule {
    fun check(item: TenderOverride): MutableList<String>
}

data class TenderOverrideArchivedGuardRule(var limit: Int) : TenderOverrideRule {
    override fun check(item: TenderOverride): MutableList<String> {
        if (item.archived && item.startsAt > limit) {
            return mutableListOf<String>("starts at ${item.startsAt} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class TenderOverrideCodeLengthRule(var maxLength: Int) : TenderOverrideRule {
    override fun check(item: TenderOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderOverrideRequiredNameRule(var message: String) : TenderOverrideRule {
    override fun check(item: TenderOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderOverrideAttemptsRangeRule(var min: Int, var max: Int) : TenderOverrideRule {
    override fun check(item: TenderOverride): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class TenderOverrideRequiredCodeRule(var message: String) : TenderOverrideRule {
    override fun check(item: TenderOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderOverrideTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : TenderOverrideRule {
    override fun check(item: TenderOverride): MutableList<String> {
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

/** Runs every rule of a tender override. */
data class TenderOverrideValidator(var rules: MutableList<TenderOverrideRule>) {
    fun validate(item: TenderOverride): MutableList<String> {
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

    fun requireValid(item: TenderOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderOverrideValidator(): TenderOverrideValidator {
    return TenderOverrideValidator(
        mutableListOf<TenderOverrideRule>(
            TenderOverrideArchivedGuardRule(32),
            TenderOverrideCodeLengthRule(1000),
            TenderOverrideRequiredNameRule("tender override needs this field"),
            TenderOverrideAttemptsRangeRule(120, 86400),
            TenderOverrideRequiredCodeRule("tender override needs this field"),
            TenderOverrideTagsLimitRule(120, mutableListOf<String>("regional")),
        ),
    )
}
