package com.acme.transport.customs.clearance.milestone

/** A rule a clearance review must pass. Check returns its problems, or none. */
interface ClearanceReviewRule {
    fun check(item: ClearanceReview): MutableList<String>
}

data class ClearanceReviewCodeLengthRule(var maxLength: Int) : ClearanceReviewRule {
    override fun check(item: ClearanceReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceReviewRequiredCodeRule(var message: String) : ClearanceReviewRule {
    override fun check(item: ClearanceReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceReviewFloorValueRangeRule(var min: Double, var max: Double) : ClearanceReviewRule {
    override fun check(item: ClearanceReview): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance review. */
data class ClearanceReviewValidator(var rules: MutableList<ClearanceReviewRule>) {
    fun validate(item: ClearanceReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ClearanceReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceReview>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceReviewValidator(): ClearanceReviewValidator {
    return ClearanceReviewValidator(
        mutableListOf<ClearanceReviewRule>(
            ClearanceReviewCodeLengthRule(16),
            ClearanceReviewRequiredCodeRule("clearance review needs this field"),
            ClearanceReviewFloorValueRangeRule(7.5, 3812.9),
        ),
    )
}
