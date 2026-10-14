package com.acme.transport.freightpay.audit.notice

import com.acme.transport.kit.text.normalize

/** A rule a audit notice must pass. Check returns its problems, or none. */
interface AuditNoticeRule {
    fun check(item: AuditNotice): MutableList<String>
}

data class AuditNoticeAllowedChannelRule(var allowed: MutableList<String>) : AuditNoticeRule {
    override fun check(item: AuditNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is missing")
        }
        return mutableListOf<String>()
    }
}

data class AuditNoticeSourceLengthRule(var maxLength: Int) : AuditNoticeRule {
    override fun check(item: AuditNotice): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditNoticeCodeLengthRule(var maxLength: Int) : AuditNoticeRule {
    override fun check(item: AuditNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit notice. */
data class AuditNoticeValidator(var rules: MutableList<AuditNoticeRule>) {
    fun validate(item: AuditNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditNoticeValidator(): AuditNoticeValidator {
    return AuditNoticeValidator(
        mutableListOf<AuditNoticeRule>(
            AuditNoticeAllowedChannelRule(mutableListOf<String>("capped", "tiered", "weighted")),
            AuditNoticeSourceLengthRule(3600),
            AuditNoticeCodeLengthRule(86400),
        ),
    )
}
