package com.acme.transport.emissions.routeoffset.offsetnotice

import com.acme.transport.kit.text.normalize

/** A rule a seasonal route offset notice must pass. Check returns its problems, or none. */
interface SeasonalRouteOffsetNoticeRule {
    fun check(item: SeasonalRouteOffsetNotice): MutableList<String>
}

data class SeasonalRouteOffsetNoticeRequiredReviewerRule(var message: String) : SeasonalRouteOffsetNoticeRule {
    override fun check(item: SeasonalRouteOffsetNotice): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRouteOffsetNoticeAllowedNameRule(
    var allowed: MutableList<String>,
) : SeasonalRouteOffsetNoticeRule {
    override fun check(item: SeasonalRouteOffsetNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRouteOffsetNoticeNameLengthRule(var maxLength: Int) : SeasonalRouteOffsetNoticeRule {
    override fun check(item: SeasonalRouteOffsetNotice): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRouteOffsetNoticeUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalRouteOffsetNoticeRule {
    override fun check(item: SeasonalRouteOffsetNotice): MutableList<String> {
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

/** Runs every rule of a seasonal route offset notice. */
data class SeasonalRouteOffsetNoticeValidator(var rules: MutableList<SeasonalRouteOffsetNoticeRule>) {
    fun validate(item: SeasonalRouteOffsetNotice): MutableList<String> {
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

    fun requireValid(item: SeasonalRouteOffsetNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRouteOffsetNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRouteOffsetNoticeValidator(): SeasonalRouteOffsetNoticeValidator {
    return SeasonalRouteOffsetNoticeValidator(
        mutableListOf<SeasonalRouteOffsetNoticeRule>(
            SeasonalRouteOffsetNoticeRequiredReviewerRule("seasonal route offset notice needs an entry"),
            SeasonalRouteOffsetNoticeAllowedNameRule(mutableListOf<String>("pending")),
            SeasonalRouteOffsetNoticeNameLengthRule(64),
            SeasonalRouteOffsetNoticeUpdatedAtRangeRule(32, 86400),
        ),
    )
}
