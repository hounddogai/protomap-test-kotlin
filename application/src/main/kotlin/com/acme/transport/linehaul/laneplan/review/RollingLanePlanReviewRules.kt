package com.acme.transport.linehaul.laneplan.review

/** A rule a rolling lane plan review must pass. Check returns its problems, or none. */
interface RollingLanePlanReviewRule {
    fun check(item: RollingLanePlanReview): MutableList<String>
}

data class RollingLanePlanReviewPalletsRangeRule(var min: Int, var max: Int) : RollingLanePlanReviewRule {
    override fun check(item: RollingLanePlanReview): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingLanePlanReviewCodePrefixRule(var prefix: String) : RollingLanePlanReviewRule {
    override fun check(item: RollingLanePlanReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingLanePlanReviewTrailerIdLengthRule(var maxLength: Int) : RollingLanePlanReviewRule {
    override fun check(item: RollingLanePlanReview): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingLanePlanReviewRequiredTitleRule(var message: String) : RollingLanePlanReviewRule {
    override fun check(item: RollingLanePlanReview): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingLanePlanReviewDriverIdLengthRule(var maxLength: Int) : RollingLanePlanReviewRule {
    override fun check(item: RollingLanePlanReview): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingLanePlanReviewDiscountRangeRule(
    var min: Double,
    var max: Double,
) : RollingLanePlanReviewRule {
    override fun check(item: RollingLanePlanReview): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling lane plan review. */
data class RollingLanePlanReviewValidator(var rules: MutableList<RollingLanePlanReviewRule>) {
    fun validate(item: RollingLanePlanReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingLanePlanReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingLanePlanReview>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingLanePlanReviewValidator(): RollingLanePlanReviewValidator {
    return RollingLanePlanReviewValidator(
        mutableListOf<RollingLanePlanReviewRule>(
            RollingLanePlanReviewPalletsRangeRule(120, 255),
            RollingLanePlanReviewCodePrefixRule("REF"),
            RollingLanePlanReviewTrailerIdLengthRule(80),
            RollingLanePlanReviewRequiredTitleRule("rolling lane plan review needs this field"),
            RollingLanePlanReviewDriverIdLengthRule(3600),
            RollingLanePlanReviewDiscountRangeRule(2.3, 3449.6),
        ),
    )
}
