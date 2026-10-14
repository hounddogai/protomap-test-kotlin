package com.acme.transport.terminal.traileryard.estimate

/** A rule a weighted trailer yard agreement must pass. Check returns its problems, or none. */
interface WeightedTrailerYardAgreementRule {
    fun check(item: WeightedTrailerYardAgreement): MutableList<String>
}

data class WeightedTrailerYardAgreementPriorityRangeRule(
    var min: Int,
    var max: Int,
) : WeightedTrailerYardAgreementRule {
    override fun check(item: WeightedTrailerYardAgreement): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class WeightedTrailerYardAgreementTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : WeightedTrailerYardAgreementRule {
    override fun check(item: WeightedTrailerYardAgreement): MutableList<String> {
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

data class WeightedTrailerYardAgreementRateRangeRule(
    var min: Double,
    var max: Double,
) : WeightedTrailerYardAgreementRule {
    override fun check(item: WeightedTrailerYardAgreement): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class WeightedTrailerYardAgreementRequiredLocaleRule(
    var message: String,
) : WeightedTrailerYardAgreementRule {
    override fun check(item: WeightedTrailerYardAgreement): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedTrailerYardAgreementLocaleLengthRule(var maxLength: Int) : WeightedTrailerYardAgreementRule {
    override fun check(item: WeightedTrailerYardAgreement): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTrailerYardAgreementRequiredLabelRule(
    var message: String,
) : WeightedTrailerYardAgreementRule {
    override fun check(item: WeightedTrailerYardAgreement): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted trailer yard agreement. */
data class WeightedTrailerYardAgreementValidator(var rules: MutableList<WeightedTrailerYardAgreementRule>) {
    fun validate(item: WeightedTrailerYardAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedTrailerYardAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedTrailerYardAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedTrailerYardAgreementValidator(): WeightedTrailerYardAgreementValidator {
    return WeightedTrailerYardAgreementValidator(
        mutableListOf<WeightedTrailerYardAgreementRule>(
            WeightedTrailerYardAgreementPriorityRangeRule(80, 255),
            WeightedTrailerYardAgreementTagsLimitRule(
                255,
                mutableListOf<String>("pending", "legacy", "seasonal"),
            ),
            WeightedTrailerYardAgreementRateRangeRule(1.6, 3390.8),
            WeightedTrailerYardAgreementRequiredLocaleRule("weighted trailer yard agreement needs this field"),
            WeightedTrailerYardAgreementLocaleLengthRule(64),
            WeightedTrailerYardAgreementRequiredLabelRule("weighted trailer yard agreement needs this field"),
        ),
    )
}
