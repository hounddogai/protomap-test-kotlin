package com.acme.transport.customs.inspection.entry

import com.acme.transport.kit.text.normalize

/** A rule a seasonal inspection bundle must pass. Check returns its problems, or none. */
interface SeasonalInspectionBundleRule {
    fun check(item: SeasonalInspectionBundle): MutableList<String>
}

data class SeasonalInspectionBundleSequenceRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalInspectionBundleRule {
    override fun check(item: SeasonalInspectionBundle): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class SeasonalInspectionBundleAllowedChecksumRule(
    var allowed: MutableList<String>,
) : SeasonalInspectionBundleRule {
    override fun check(item: SeasonalInspectionBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalInspectionBundleActiveGuardRule(var limit: Int) : SeasonalInspectionBundleRule {
    override fun check(item: SeasonalInspectionBundle): MutableList<String> {
        if (item.active && item.limit > limit) {
            return mutableListOf<String>("limit ${item.limit} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalInspectionBundleStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalInspectionBundleRule {
    override fun check(item: SeasonalInspectionBundle): MutableList<String> {
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

data class SeasonalInspectionBundleDiscountRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalInspectionBundleRule {
    override fun check(item: SeasonalInspectionBundle): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class SeasonalInspectionBundleRequiredNameRule(var message: String) : SeasonalInspectionBundleRule {
    override fun check(item: SeasonalInspectionBundle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal inspection bundle. */
data class SeasonalInspectionBundleValidator(var rules: MutableList<SeasonalInspectionBundleRule>) {
    fun validate(item: SeasonalInspectionBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalInspectionBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalInspectionBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalInspectionBundleValidator(): SeasonalInspectionBundleValidator {
    return SeasonalInspectionBundleValidator(
        mutableListOf<SeasonalInspectionBundleRule>(
            SeasonalInspectionBundleSequenceRangeRule(80, 1000),
            SeasonalInspectionBundleAllowedChecksumRule(mutableListOf<String>("adaptive", "adaptive")),
            SeasonalInspectionBundleActiveGuardRule(32),
            SeasonalInspectionBundleStartsAtRangeRule(16, 32),
            SeasonalInspectionBundleDiscountRangeRule(8.7, 4974.7),
            SeasonalInspectionBundleRequiredNameRule("seasonal inspection bundle needs an entry"),
        ),
    )
}
