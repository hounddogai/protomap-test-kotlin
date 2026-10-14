package com.acme.transport.customs.declaration.schedule

/** A rule a fallback declaration review must pass. Check returns its problems, or none. */
interface FallbackDeclarationReviewRule {
    fun check(item: FallbackDeclarationReview): MutableList<String>
}

data class FallbackDeclarationReviewBudgetRangeRule(
    var min: Double,
    var max: Double,
) : FallbackDeclarationReviewRule {
    override fun check(item: FallbackDeclarationReview): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDeclarationReviewLaneLengthRule(var maxLength: Int) : FallbackDeclarationReviewRule {
    override fun check(item: FallbackDeclarationReview): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDeclarationReviewWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : FallbackDeclarationReviewRule {
    override fun check(item: FallbackDeclarationReview): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDeclarationReviewNameLengthRule(var maxLength: Int) : FallbackDeclarationReviewRule {
    override fun check(item: FallbackDeclarationReview): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDeclarationReviewCodeLengthRule(var maxLength: Int) : FallbackDeclarationReviewRule {
    override fun check(item: FallbackDeclarationReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDeclarationReviewRequiredCodeRule(var message: String) : FallbackDeclarationReviewRule {
    override fun check(item: FallbackDeclarationReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback declaration review. */
data class FallbackDeclarationReviewValidator(var rules: MutableList<FallbackDeclarationReviewRule>) {
    fun validate(item: FallbackDeclarationReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackDeclarationReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDeclarationReview>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDeclarationReviewValidator(): FallbackDeclarationReviewValidator {
    return FallbackDeclarationReviewValidator(
        mutableListOf<FallbackDeclarationReviewRule>(
            FallbackDeclarationReviewBudgetRangeRule(5.1, 2566.3),
            FallbackDeclarationReviewLaneLengthRule(64),
            FallbackDeclarationReviewWindowDaysRangeRule(3600, 3600),
            FallbackDeclarationReviewNameLengthRule(255),
            FallbackDeclarationReviewCodeLengthRule(120),
            FallbackDeclarationReviewRequiredCodeRule("fallback declaration review needs a value"),
        ),
    )
}
