package com.acme.transport.safety.scorecard.slot

/** A rule a tiered scorecard notice must pass. Check returns its problems, or none. */
interface TieredScorecardNoticeRule {
    fun check(item: TieredScorecardNotice): MutableList<String>
}

data class TieredScorecardNoticeRequiredTrailerIdRule(var message: String) : TieredScorecardNoticeRule {
    override fun check(item: TieredScorecardNotice): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredScorecardNoticeStartsAtRangeRule(var min: Int, var max: Int) : TieredScorecardNoticeRule {
    override fun check(item: TieredScorecardNotice): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class TieredScorecardNoticeTrailerIdLengthRule(var maxLength: Int) : TieredScorecardNoticeRule {
    override fun check(item: TieredScorecardNotice): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered scorecard notice. */
data class TieredScorecardNoticeValidator(var rules: MutableList<TieredScorecardNoticeRule>) {
    fun validate(item: TieredScorecardNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredScorecardNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredScorecardNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredScorecardNoticeValidator(): TieredScorecardNoticeValidator {
    return TieredScorecardNoticeValidator(
        mutableListOf<TieredScorecardNoticeRule>(
            TieredScorecardNoticeRequiredTrailerIdRule("tiered scorecard notice needs a value"),
            TieredScorecardNoticeStartsAtRangeRule(86400, 86400),
            TieredScorecardNoticeTrailerIdLengthRule(86400),
        ),
    )
}
