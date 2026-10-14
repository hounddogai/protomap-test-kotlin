package com.acme.transport.emissions.fuelburn.notice

import com.acme.transport.kit.text.normalize

/** A rule a fuel burn summary must pass. Check returns its problems, or none. */
interface FuelBurnSummaryRule {
    fun check(item: FuelBurnSummary): MutableList<String>
}

data class FuelBurnSummaryDescriptionLengthRule(var maxLength: Int) : FuelBurnSummaryRule {
    override fun check(item: FuelBurnSummary): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnSummaryAllowedDescriptionRule(var allowed: MutableList<String>) : FuelBurnSummaryRule {
    override fun check(item: FuelBurnSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is missing")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnSummaryLabelLengthRule(var maxLength: Int) : FuelBurnSummaryRule {
    override fun check(item: FuelBurnSummary): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnSummaryRequiredLabelRule(var message: String) : FuelBurnSummaryRule {
    override fun check(item: FuelBurnSummary): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnSummaryTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : FuelBurnSummaryRule {
    override fun check(item: FuelBurnSummary): MutableList<String> {
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

data class FuelBurnSummaryRequiredDescriptionRule(var message: String) : FuelBurnSummaryRule {
    override fun check(item: FuelBurnSummary): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn summary. */
data class FuelBurnSummaryValidator(var rules: MutableList<FuelBurnSummaryRule>) {
    fun validate(item: FuelBurnSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelBurnSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnSummaryValidator(): FuelBurnSummaryValidator {
    return FuelBurnSummaryValidator(
        mutableListOf<FuelBurnSummaryRule>(
            FuelBurnSummaryDescriptionLengthRule(86400),
            FuelBurnSummaryAllowedDescriptionRule(mutableListOf<String>("strict", "legacy")),
            FuelBurnSummaryLabelLengthRule(16),
            FuelBurnSummaryRequiredLabelRule("fuel burn summary needs an entry"),
            FuelBurnSummaryTagsLimitRule(32, mutableListOf<String>("adaptive", "strict", "capped")),
            FuelBurnSummaryRequiredDescriptionRule("fuel burn summary needs an entry"),
        ),
    )
}
