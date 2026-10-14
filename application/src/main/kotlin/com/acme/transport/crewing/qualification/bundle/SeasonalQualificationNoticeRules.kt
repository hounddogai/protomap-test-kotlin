package com.acme.transport.crewing.qualification.bundle

import com.acme.transport.kit.text.normalize

/** A rule a seasonal qualification notice must pass. Check returns its problems, or none. */
interface SeasonalQualificationNoticeRule {
    fun check(item: SeasonalQualificationNotice): MutableList<String>
}

data class SeasonalQualificationNoticeCodeLengthRule(var maxLength: Int) : SeasonalQualificationNoticeRule {
    override fun check(item: SeasonalQualificationNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalQualificationNoticeRequiredRegionRule(
    var message: String,
) : SeasonalQualificationNoticeRule {
    override fun check(item: SeasonalQualificationNotice): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalQualificationNoticeCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalQualificationNoticeRule {
    override fun check(item: SeasonalQualificationNotice): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class SeasonalQualificationNoticeAllowedLabelRule(
    var allowed: MutableList<String>,
) : SeasonalQualificationNoticeRule {
    override fun check(item: SeasonalQualificationNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal qualification notice. */
data class SeasonalQualificationNoticeValidator(var rules: MutableList<SeasonalQualificationNoticeRule>) {
    fun validate(item: SeasonalQualificationNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalQualificationNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalQualificationNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalQualificationNoticeValidator(): SeasonalQualificationNoticeValidator {
    return SeasonalQualificationNoticeValidator(
        mutableListOf<SeasonalQualificationNoticeRule>(
            SeasonalQualificationNoticeCodeLengthRule(64),
            SeasonalQualificationNoticeRequiredRegionRule("seasonal qualification notice needs an entry"),
            SeasonalQualificationNoticeCreatedAtRangeRule(1000, 86400),
            SeasonalQualificationNoticeAllowedLabelRule(mutableListOf<String>()),
        ),
    )
}
