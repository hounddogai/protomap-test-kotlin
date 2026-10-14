package com.acme.transport.network.transittime.notice

import com.acme.transport.kit.text.normalize

/** A rule a tiered transit time notice must pass. Check returns its problems, or none. */
interface TieredTransitTimeNoticeRule {
    fun check(item: TieredTransitTimeNotice): MutableList<String>
}

data class TieredTransitTimeNoticeAllowedTitleRule(
    var allowed: MutableList<String>,
) : TieredTransitTimeNoticeRule {
    override fun check(item: TieredTransitTimeNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class TieredTransitTimeNoticeCategoryLengthRule(var maxLength: Int) : TieredTransitTimeNoticeRule {
    override fun check(item: TieredTransitTimeNotice): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredTransitTimeNoticeRequiredTitleRule(var message: String) : TieredTransitTimeNoticeRule {
    override fun check(item: TieredTransitTimeNotice): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTransitTimeNoticeTitleLengthRule(var maxLength: Int) : TieredTransitTimeNoticeRule {
    override fun check(item: TieredTransitTimeNotice): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered transit time notice. */
data class TieredTransitTimeNoticeValidator(var rules: MutableList<TieredTransitTimeNoticeRule>) {
    fun validate(item: TieredTransitTimeNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredTransitTimeNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredTransitTimeNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredTransitTimeNoticeValidator(): TieredTransitTimeNoticeValidator {
    return TieredTransitTimeNoticeValidator(
        mutableListOf<TieredTransitTimeNoticeRule>(
            TieredTransitTimeNoticeAllowedTitleRule(mutableListOf<String>("fallback")),
            TieredTransitTimeNoticeCategoryLengthRule(120),
            TieredTransitTimeNoticeRequiredTitleRule("tiered transit time notice needs this field"),
            TieredTransitTimeNoticeTitleLengthRule(16),
        ),
    )
}
