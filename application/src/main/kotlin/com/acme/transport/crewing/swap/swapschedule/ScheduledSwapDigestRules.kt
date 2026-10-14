package com.acme.transport.crewing.swap.swapschedule

/** A rule a scheduled swap digest must pass. Check returns its problems, or none. */
interface ScheduledSwapDigestRule {
    fun check(item: ScheduledSwapDigest): MutableList<String>
}

data class ScheduledSwapDigestMaximumRangeRule(var min: Double, var max: Double) : ScheduledSwapDigestRule {
    override fun check(item: ScheduledSwapDigest): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledSwapDigestRatioRangeRule(var min: Double, var max: Double) : ScheduledSwapDigestRule {
    override fun check(item: ScheduledSwapDigest): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledSwapDigestDiscountRangeRule(var min: Double, var max: Double) : ScheduledSwapDigestRule {
    override fun check(item: ScheduledSwapDigest): MutableList<String> {
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

data class ScheduledSwapDigestRequiredTitleRule(var message: String) : ScheduledSwapDigestRule {
    override fun check(item: ScheduledSwapDigest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledSwapDigestNameLengthRule(var maxLength: Int) : ScheduledSwapDigestRule {
    override fun check(item: ScheduledSwapDigest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledSwapDigestDepotLengthRule(var maxLength: Int) : ScheduledSwapDigestRule {
    override fun check(item: ScheduledSwapDigest): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled swap digest. */
data class ScheduledSwapDigestValidator(var rules: MutableList<ScheduledSwapDigestRule>) {
    fun validate(item: ScheduledSwapDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledSwapDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledSwapDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledSwapDigestValidator(): ScheduledSwapDigestValidator {
    return ScheduledSwapDigestValidator(
        mutableListOf<ScheduledSwapDigestRule>(
            ScheduledSwapDigestMaximumRangeRule(9.0, 2072.9),
            ScheduledSwapDigestRatioRangeRule(4.8, 2319.6),
            ScheduledSwapDigestDiscountRangeRule(0.2, 2856.0),
            ScheduledSwapDigestRequiredTitleRule("scheduled swap digest needs an entry"),
            ScheduledSwapDigestNameLengthRule(80),
            ScheduledSwapDigestDepotLengthRule(1000),
        ),
    )
}
