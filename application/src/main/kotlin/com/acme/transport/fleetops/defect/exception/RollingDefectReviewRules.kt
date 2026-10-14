package com.acme.transport.fleetops.defect.exception

/** A rule a rolling defect review must pass. Check returns its problems, or none. */
interface RollingDefectReviewRule {
    fun check(item: RollingDefectReview): MutableList<String>
}

data class RollingDefectReviewCodeLengthRule(var maxLength: Int) : RollingDefectReviewRule {
    override fun check(item: RollingDefectReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDefectReviewRequiredOwnerIdRule(var message: String) : RollingDefectReviewRule {
    override fun check(item: RollingDefectReview): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDefectReviewWeightRangeRule(var min: Double, var max: Double) : RollingDefectReviewRule {
    override fun check(item: RollingDefectReview): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class RollingDefectReviewTitleLengthRule(var maxLength: Int) : RollingDefectReviewRule {
    override fun check(item: RollingDefectReview): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling defect review. */
data class RollingDefectReviewValidator(var rules: MutableList<RollingDefectReviewRule>) {
    fun validate(item: RollingDefectReview): MutableList<String> {
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

    fun requireValid(item: RollingDefectReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDefectReview>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDefectReviewValidator(): RollingDefectReviewValidator {
    return RollingDefectReviewValidator(
        mutableListOf<RollingDefectReviewRule>(
            RollingDefectReviewCodeLengthRule(120),
            RollingDefectReviewRequiredOwnerIdRule("rolling defect review needs an entry"),
            RollingDefectReviewWeightRangeRule(0.5, 182.2),
            RollingDefectReviewTitleLengthRule(64),
        ),
    )
}
