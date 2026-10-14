package com.acme.transport.crewing.roster.rosteragreement

/** A rule a capped roster notice must pass. Check returns its problems, or none. */
interface CappedRosterNoticeRule {
    fun check(item: CappedRosterNotice): MutableList<String>
}

data class CappedRosterNoticeCarrierLengthRule(var maxLength: Int) : CappedRosterNoticeRule {
    override fun check(item: CappedRosterNotice): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRosterNoticeRequiredCodeRule(var message: String) : CappedRosterNoticeRule {
    override fun check(item: CappedRosterNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedRosterNoticeTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : CappedRosterNoticeRule {
    override fun check(item: CappedRosterNotice): MutableList<String> {
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

data class CappedRosterNoticeLabelLengthRule(var maxLength: Int) : CappedRosterNoticeRule {
    override fun check(item: CappedRosterNotice): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped roster notice. */
data class CappedRosterNoticeValidator(var rules: MutableList<CappedRosterNoticeRule>) {
    fun validate(item: CappedRosterNotice): MutableList<String> {
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

    fun requireValid(item: CappedRosterNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedRosterNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedRosterNoticeValidator(): CappedRosterNoticeValidator {
    return CappedRosterNoticeValidator(
        mutableListOf<CappedRosterNoticeRule>(
            CappedRosterNoticeCarrierLengthRule(16),
            CappedRosterNoticeRequiredCodeRule("capped roster notice needs an entry"),
            CappedRosterNoticeTagsLimitRule(3600, mutableListOf<String>("manual", "tiered", "lenient")),
            CappedRosterNoticeLabelLengthRule(120),
        ),
    )
}
