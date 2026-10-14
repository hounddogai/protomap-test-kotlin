package com.acme.transport.linehaul.laneplan.planbundle

/** A rule a lane plan review must pass. Check returns its problems, or none. */
interface LanePlanReviewRule {
    fun check(item: LanePlanReview): MutableList<String>
}

data class LanePlanReviewRequiredLabelRule(var message: String) : LanePlanReviewRule {
    override fun check(item: LanePlanReview): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanReviewLabelLengthRule(var maxLength: Int) : LanePlanReviewRule {
    override fun check(item: LanePlanReview): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanReviewRequiredCodeRule(var message: String) : LanePlanReviewRule {
    override fun check(item: LanePlanReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan review. */
data class LanePlanReviewValidator(var rules: MutableList<LanePlanReviewRule>) {
    fun validate(item: LanePlanReview): MutableList<String> {
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

    fun requireValid(item: LanePlanReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanReview>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanReviewValidator(): LanePlanReviewValidator {
    return LanePlanReviewValidator(
        mutableListOf<LanePlanReviewRule>(
            LanePlanReviewRequiredLabelRule("lane plan review needs an entry"),
            LanePlanReviewLabelLengthRule(80),
            LanePlanReviewRequiredCodeRule("lane plan review needs a value"),
        ),
    )
}
