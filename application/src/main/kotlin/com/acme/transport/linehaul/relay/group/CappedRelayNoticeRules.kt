package com.acme.transport.linehaul.relay.group

import com.acme.transport.kit.text.normalize

/** A rule a capped relay notice must pass. Check returns its problems, or none. */
interface CappedRelayNoticeRule {
    fun check(item: CappedRelayNotice): MutableList<String>
}

data class CappedRelayNoticeTitleLengthRule(var maxLength: Int) : CappedRelayNoticeRule {
    override fun check(item: CappedRelayNotice): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRelayNoticeAllowedTitleRule(var allowed: MutableList<String>) : CappedRelayNoticeRule {
    override fun check(item: CappedRelayNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class CappedRelayNoticeTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : CappedRelayNoticeRule {
    override fun check(item: CappedRelayNotice): MutableList<String> {
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

/** Runs every rule of a capped relay notice. */
data class CappedRelayNoticeValidator(var rules: MutableList<CappedRelayNoticeRule>) {
    fun validate(item: CappedRelayNotice): MutableList<String> {
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

    fun requireValid(item: CappedRelayNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedRelayNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedRelayNoticeValidator(): CappedRelayNoticeValidator {
    return CappedRelayNoticeValidator(
        mutableListOf<CappedRelayNoticeRule>(
            CappedRelayNoticeTitleLengthRule(3600),
            CappedRelayNoticeAllowedTitleRule(mutableListOf<String>("batched", "weighted", "scheduled")),
            CappedRelayNoticeTagsLimitRule(120, mutableListOf<String>("primary")),
        ),
    )
}
