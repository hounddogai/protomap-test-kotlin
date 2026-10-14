package com.acme.transport.crewing.roster.variance

/** A rule a roster notice must pass. Check returns its problems, or none. */
interface RosterNoticeRule {
    fun check(item: RosterNotice): MutableList<String>
}

data class RosterNoticeTagsLimitRule(var maxCount: Int, var banned: MutableList<String>) : RosterNoticeRule {
    override fun check(item: RosterNotice): MutableList<String> {
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

data class RosterNoticeCodePrefixRule(var prefix: String) : RosterNoticeRule {
    override fun check(item: RosterNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RosterNoticeTitleLengthRule(var maxLength: Int) : RosterNoticeRule {
    override fun check(item: RosterNotice): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterNoticeRequiredTitleRule(var message: String) : RosterNoticeRule {
    override fun check(item: RosterNotice): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster notice. */
data class RosterNoticeValidator(var rules: MutableList<RosterNoticeRule>) {
    fun validate(item: RosterNotice): MutableList<String> {
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

    fun requireValid(item: RosterNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterNoticeValidator(): RosterNoticeValidator {
    return RosterNoticeValidator(
        mutableListOf<RosterNoticeRule>(
            RosterNoticeTagsLimitRule(80, mutableListOf<String>()),
            RosterNoticeCodePrefixRule("REF"),
            RosterNoticeTitleLengthRule(1000),
            RosterNoticeRequiredTitleRule("roster notice needs a value"),
        ),
    )
}
