package com.acme.transport.freightpay.audit.batch

import com.acme.transport.kit.text.normalize

/** A rule a default audit notice must pass. Check returns its problems, or none. */
interface DefaultAuditNoticeRule {
    fun check(item: DefaultAuditNotice): MutableList<String>
}

data class DefaultAuditNoticePalletsRangeRule(var min: Int, var max: Int) : DefaultAuditNoticeRule {
    override fun check(item: DefaultAuditNotice): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAuditNoticeRatioRangeRule(var min: Double, var max: Double) : DefaultAuditNoticeRule {
    override fun check(item: DefaultAuditNotice): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAuditNoticeLimitRangeRule(var min: Int, var max: Int) : DefaultAuditNoticeRule {
    override fun check(item: DefaultAuditNotice): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class DefaultAuditNoticeAllowedTitleRule(var allowed: MutableList<String>) : DefaultAuditNoticeRule {
    override fun check(item: DefaultAuditNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAuditNoticeRequiredTitleRule(var message: String) : DefaultAuditNoticeRule {
    override fun check(item: DefaultAuditNotice): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default audit notice. */
data class DefaultAuditNoticeValidator(var rules: MutableList<DefaultAuditNoticeRule>) {
    fun validate(item: DefaultAuditNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultAuditNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultAuditNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultAuditNoticeValidator(): DefaultAuditNoticeValidator {
    return DefaultAuditNoticeValidator(
        mutableListOf<DefaultAuditNoticeRule>(
            DefaultAuditNoticePalletsRangeRule(64, 1000),
            DefaultAuditNoticeRatioRangeRule(6.8, 818.7),
            DefaultAuditNoticeLimitRangeRule(32, 120),
            DefaultAuditNoticeAllowedTitleRule(mutableListOf<String>()),
            DefaultAuditNoticeRequiredTitleRule("default audit notice needs an entry"),
        ),
    )
}
