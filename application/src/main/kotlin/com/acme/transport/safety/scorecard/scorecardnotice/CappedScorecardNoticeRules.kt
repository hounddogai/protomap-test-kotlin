package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.kit.text.normalize

/** A rule a capped scorecard notice must pass. Check returns its problems, or none. */
interface CappedScorecardNoticeRule {
    fun check(item: CappedScorecardNotice): MutableList<String>
}

data class CappedScorecardNoticeAllowedLabelRule(var allowed: MutableList<String>) : CappedScorecardNoticeRule {
    override fun check(item: CappedScorecardNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class CappedScorecardNoticeUpdatedAtRangeRule(var min: Int, var max: Int) : CappedScorecardNoticeRule {
    override fun check(item: CappedScorecardNotice): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class CappedScorecardNoticeRequiredCodeRule(var message: String) : CappedScorecardNoticeRule {
    override fun check(item: CappedScorecardNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped scorecard notice. */
data class CappedScorecardNoticeValidator(var rules: MutableList<CappedScorecardNoticeRule>) {
    fun validate(item: CappedScorecardNotice): MutableList<String> {
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

    fun requireValid(item: CappedScorecardNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedScorecardNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedScorecardNoticeValidator(): CappedScorecardNoticeValidator {
    return CappedScorecardNoticeValidator(
        mutableListOf<CappedScorecardNoticeRule>(
            CappedScorecardNoticeAllowedLabelRule(mutableListOf<String>("manual", "fallback")),
            CappedScorecardNoticeUpdatedAtRangeRule(32, 86400),
            CappedScorecardNoticeRequiredCodeRule("capped scorecard notice needs a value"),
        ),
    )
}
