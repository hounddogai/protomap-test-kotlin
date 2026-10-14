package com.acme.transport.customs.inspection.threshold

import com.acme.transport.kit.text.normalize

/** A rule a weighted inspection bundle must pass. Check returns its problems, or none. */
interface WeightedInspectionBundleRule {
    fun check(item: WeightedInspectionBundle): MutableList<String>
}

data class WeightedInspectionBundleTitleLengthRule(var maxLength: Int) : WeightedInspectionBundleRule {
    override fun check(item: WeightedInspectionBundle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionBundleCostRangeRule(
    var min: Double,
    var max: Double,
) : WeightedInspectionBundleRule {
    override fun check(item: WeightedInspectionBundle): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class WeightedInspectionBundleTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : WeightedInspectionBundleRule {
    override fun check(item: WeightedInspectionBundle): MutableList<String> {
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

data class WeightedInspectionBundleAllowedShiftCodeRule(
    var allowed: MutableList<String>,
) : WeightedInspectionBundleRule {
    override fun check(item: WeightedInspectionBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is too long")
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionBundleEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : WeightedInspectionBundleRule {
    override fun check(item: WeightedInspectionBundle): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class WeightedInspectionBundleRequiredTitleRule(var message: String) : WeightedInspectionBundleRule {
    override fun check(item: WeightedInspectionBundle): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted inspection bundle. */
data class WeightedInspectionBundleValidator(var rules: MutableList<WeightedInspectionBundleRule>) {
    fun validate(item: WeightedInspectionBundle): MutableList<String> {
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

    fun requireValid(item: WeightedInspectionBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedInspectionBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedInspectionBundleValidator(): WeightedInspectionBundleValidator {
    return WeightedInspectionBundleValidator(
        mutableListOf<WeightedInspectionBundleRule>(
            WeightedInspectionBundleTitleLengthRule(3600),
            WeightedInspectionBundleCostRangeRule(8.5, 652.1),
            WeightedInspectionBundleTagsLimitRule(64, mutableListOf<String>()),
            WeightedInspectionBundleAllowedShiftCodeRule(mutableListOf<String>("pending", "primary", "global")),
            WeightedInspectionBundleEtaMinutesRangeRule(86400, 86400),
            WeightedInspectionBundleRequiredTitleRule("weighted inspection bundle needs this field"),
        ),
    )
}
