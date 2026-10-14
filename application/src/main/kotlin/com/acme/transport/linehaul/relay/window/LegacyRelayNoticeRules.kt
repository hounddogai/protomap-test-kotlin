package com.acme.transport.linehaul.relay.window

import com.acme.transport.kit.text.normalize

/** A rule a legacy relay notice must pass. Check returns its problems, or none. */
interface LegacyRelayNoticeRule {
    fun check(item: LegacyRelayNotice): MutableList<String>
}

data class LegacyRelayNoticeAllowedTitleRule(var allowed: MutableList<String>) : LegacyRelayNoticeRule {
    override fun check(item: LegacyRelayNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is stale")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRelayNoticeTitleLengthRule(var maxLength: Int) : LegacyRelayNoticeRule {
    override fun check(item: LegacyRelayNotice): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRelayNoticeTrailerIdLengthRule(var maxLength: Int) : LegacyRelayNoticeRule {
    override fun check(item: LegacyRelayNotice): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy relay notice. */
data class LegacyRelayNoticeValidator(var rules: MutableList<LegacyRelayNoticeRule>) {
    fun validate(item: LegacyRelayNotice): MutableList<String> {
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

    fun requireValid(item: LegacyRelayNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyRelayNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyRelayNoticeValidator(): LegacyRelayNoticeValidator {
    return LegacyRelayNoticeValidator(
        mutableListOf<LegacyRelayNoticeRule>(
            LegacyRelayNoticeAllowedTitleRule(mutableListOf<String>("scheduled", "strict", "global")),
            LegacyRelayNoticeTitleLengthRule(255),
            LegacyRelayNoticeTrailerIdLengthRule(3600),
        ),
    )
}
