package com.acme.transport.lastmile.drop.dropslot

/** A rule a seasonal drop agreement must pass. Check returns its problems, or none. */
interface SeasonalDropAgreementRule {
    fun check(item: SeasonalDropAgreement): MutableList<String>
}

data class SeasonalDropAgreementThresholdRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalDropAgreementRule {
    override fun check(item: SeasonalDropAgreement): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class SeasonalDropAgreementRequiredCodeRule(var message: String) : SeasonalDropAgreementRule {
    override fun check(item: SeasonalDropAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDropAgreementMarginRangeRule(var min: Double, var max: Double) : SeasonalDropAgreementRule {
    override fun check(item: SeasonalDropAgreement): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDropAgreementTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : SeasonalDropAgreementRule {
    override fun check(item: SeasonalDropAgreement): MutableList<String> {
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

/** Runs every rule of a seasonal drop agreement. */
data class SeasonalDropAgreementValidator(var rules: MutableList<SeasonalDropAgreementRule>) {
    fun validate(item: SeasonalDropAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalDropAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDropAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDropAgreementValidator(): SeasonalDropAgreementValidator {
    return SeasonalDropAgreementValidator(
        mutableListOf<SeasonalDropAgreementRule>(
            SeasonalDropAgreementThresholdRangeRule(0.1, 4076.2),
            SeasonalDropAgreementRequiredCodeRule("seasonal drop agreement needs an entry"),
            SeasonalDropAgreementMarginRangeRule(8.0, 2598.9),
            SeasonalDropAgreementTagsLimitRule(1000, mutableListOf<String>("default", "weighted", "rolling")),
        ),
    )
}
