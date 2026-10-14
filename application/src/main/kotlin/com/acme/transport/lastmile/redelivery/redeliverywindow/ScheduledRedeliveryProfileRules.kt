package com.acme.transport.lastmile.redelivery.redeliverywindow

import com.acme.transport.kit.text.normalize

/** A rule a scheduled redelivery profile must pass. Check returns its problems, or none. */
interface ScheduledRedeliveryProfileRule {
    fun check(item: ScheduledRedeliveryProfile): MutableList<String>
}

data class ScheduledRedeliveryProfileTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : ScheduledRedeliveryProfileRule {
    override fun check(item: ScheduledRedeliveryProfile): MutableList<String> {
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

data class ScheduledRedeliveryProfileRequiredTitleRule(var message: String) : ScheduledRedeliveryProfileRule {
    override fun check(item: ScheduledRedeliveryProfile): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryProfileAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledRedeliveryProfileRule {
    override fun check(item: ScheduledRedeliveryProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryProfileRequiredChecksumRule(
    var message: String,
) : ScheduledRedeliveryProfileRule {
    override fun check(item: ScheduledRedeliveryProfile): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled redelivery profile. */
data class ScheduledRedeliveryProfileValidator(var rules: MutableList<ScheduledRedeliveryProfileRule>) {
    fun validate(item: ScheduledRedeliveryProfile): MutableList<String> {
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

    fun requireValid(item: ScheduledRedeliveryProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRedeliveryProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRedeliveryProfileValidator(): ScheduledRedeliveryProfileValidator {
    return ScheduledRedeliveryProfileValidator(
        mutableListOf<ScheduledRedeliveryProfileRule>(
            ScheduledRedeliveryProfileTagsLimitRule(32, mutableListOf<String>("legacy", "strict", "primary")),
            ScheduledRedeliveryProfileRequiredTitleRule("scheduled redelivery profile needs an entry"),
            ScheduledRedeliveryProfileAllowedCodeRule(mutableListOf<String>()),
            ScheduledRedeliveryProfileRequiredChecksumRule("scheduled redelivery profile needs an entry"),
        ),
    )
}
