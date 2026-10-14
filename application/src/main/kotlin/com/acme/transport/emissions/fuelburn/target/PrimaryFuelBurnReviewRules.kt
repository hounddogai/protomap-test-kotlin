package com.acme.transport.emissions.fuelburn.target

/** A rule a primary fuel burn review must pass. Check returns its problems, or none. */
interface PrimaryFuelBurnReviewRule {
    fun check(item: PrimaryFuelBurnReview): MutableList<String>
}

data class PrimaryFuelBurnReviewRequiredTimezoneRule(var message: String) : PrimaryFuelBurnReviewRule {
    override fun check(item: PrimaryFuelBurnReview): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelBurnReviewCodeLengthRule(var maxLength: Int) : PrimaryFuelBurnReviewRule {
    override fun check(item: PrimaryFuelBurnReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelBurnReviewCodePrefixRule(var prefix: String) : PrimaryFuelBurnReviewRule {
    override fun check(item: PrimaryFuelBurnReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelBurnReviewRequiredTitleRule(var message: String) : PrimaryFuelBurnReviewRule {
    override fun check(item: PrimaryFuelBurnReview): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelBurnReviewTitleLengthRule(var maxLength: Int) : PrimaryFuelBurnReviewRule {
    override fun check(item: PrimaryFuelBurnReview): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelBurnReviewTimezoneLengthRule(var maxLength: Int) : PrimaryFuelBurnReviewRule {
    override fun check(item: PrimaryFuelBurnReview): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary fuel burn review. */
data class PrimaryFuelBurnReviewValidator(var rules: MutableList<PrimaryFuelBurnReviewRule>) {
    fun validate(item: PrimaryFuelBurnReview): MutableList<String> {
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

    fun requireValid(item: PrimaryFuelBurnReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryFuelBurnReview>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryFuelBurnReviewValidator(): PrimaryFuelBurnReviewValidator {
    return PrimaryFuelBurnReviewValidator(
        mutableListOf<PrimaryFuelBurnReviewRule>(
            PrimaryFuelBurnReviewRequiredTimezoneRule("primary fuel burn review needs a value"),
            PrimaryFuelBurnReviewCodeLengthRule(120),
            PrimaryFuelBurnReviewCodePrefixRule("X"),
            PrimaryFuelBurnReviewRequiredTitleRule("primary fuel burn review needs this field"),
            PrimaryFuelBurnReviewTitleLengthRule(120),
            PrimaryFuelBurnReviewTimezoneLengthRule(80),
        ),
    )
}
