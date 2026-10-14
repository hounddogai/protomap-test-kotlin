package com.acme.transport.freightpay.ratecard.summary

/** A rule a capped rate card bundle must pass. Check returns its problems, or none. */
interface CappedRateCardBundleRule {
    fun check(item: CappedRateCardBundle): MutableList<String>
}

data class CappedRateCardBundleRequiredNotesRule(var message: String) : CappedRateCardBundleRule {
    override fun check(item: CappedRateCardBundle): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardBundleCategoryLengthRule(var maxLength: Int) : CappedRateCardBundleRule {
    override fun check(item: CappedRateCardBundle): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardBundleNotesLengthRule(var maxLength: Int) : CappedRateCardBundleRule {
    override fun check(item: CappedRateCardBundle): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardBundleRequiredTitleRule(var message: String) : CappedRateCardBundleRule {
    override fun check(item: CappedRateCardBundle): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardBundleFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : CappedRateCardBundleRule {
    override fun check(item: CappedRateCardBundle): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardBundleScoreRangeRule(var min: Double, var max: Double) : CappedRateCardBundleRule {
    override fun check(item: CappedRateCardBundle): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped rate card bundle. */
data class CappedRateCardBundleValidator(var rules: MutableList<CappedRateCardBundleRule>) {
    fun validate(item: CappedRateCardBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedRateCardBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedRateCardBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedRateCardBundleValidator(): CappedRateCardBundleValidator {
    return CappedRateCardBundleValidator(
        mutableListOf<CappedRateCardBundleRule>(
            CappedRateCardBundleRequiredNotesRule("capped rate card bundle needs an entry"),
            CappedRateCardBundleCategoryLengthRule(32),
            CappedRateCardBundleNotesLengthRule(120),
            CappedRateCardBundleRequiredTitleRule("capped rate card bundle needs a value"),
            CappedRateCardBundleFuelLitersRangeRule(6.5, 3404.8),
            CappedRateCardBundleScoreRangeRule(6.4, 2794.4),
        ),
    )
}
