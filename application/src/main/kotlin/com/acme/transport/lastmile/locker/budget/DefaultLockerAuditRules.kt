package com.acme.transport.lastmile.locker.budget

import com.acme.transport.kit.text.normalize

/** A rule a default locker audit must pass. Check returns its problems, or none. */
interface DefaultLockerAuditRule {
    fun check(item: DefaultLockerAudit): MutableList<String>
}

data class DefaultLockerAuditTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : DefaultLockerAuditRule {
    override fun check(item: DefaultLockerAudit): MutableList<String> {
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

data class DefaultLockerAuditAllowedSegmentRule(var allowed: MutableList<String>) : DefaultLockerAuditRule {
    override fun check(item: DefaultLockerAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class DefaultLockerAuditSegmentLengthRule(var maxLength: Int) : DefaultLockerAuditRule {
    override fun check(item: DefaultLockerAudit): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default locker audit. */
data class DefaultLockerAuditValidator(var rules: MutableList<DefaultLockerAuditRule>) {
    fun validate(item: DefaultLockerAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultLockerAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultLockerAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultLockerAuditValidator(): DefaultLockerAuditValidator {
    return DefaultLockerAuditValidator(
        mutableListOf<DefaultLockerAuditRule>(
            DefaultLockerAuditTagsLimitRule(1000, mutableListOf<String>("manual", "weighted", "default")),
            DefaultLockerAuditAllowedSegmentRule(mutableListOf<String>("fallback", "regional", "scheduled")),
            DefaultLockerAuditSegmentLengthRule(120),
        ),
    )
}
