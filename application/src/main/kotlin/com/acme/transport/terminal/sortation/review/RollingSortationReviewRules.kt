package com.acme.transport.terminal.sortation.review

/** A rule a rolling sortation review must pass. Check returns its problems, or none. */
interface RollingSortationReviewRule {
    fun check(item: RollingSortationReview): MutableList<String>
}

data class RollingSortationReviewCodePrefixRule(var prefix: String) : RollingSortationReviewRule {
    override fun check(item: RollingSortationReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingSortationReviewBatchSizeRangeRule(var min: Int, var max: Int) : RollingSortationReviewRule {
    override fun check(item: RollingSortationReview): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingSortationReviewLabelLengthRule(var maxLength: Int) : RollingSortationReviewRule {
    override fun check(item: RollingSortationReview): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingSortationReviewSummaryLengthRule(var maxLength: Int) : RollingSortationReviewRule {
    override fun check(item: RollingSortationReview): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling sortation review. */
data class RollingSortationReviewValidator(var rules: MutableList<RollingSortationReviewRule>) {
    fun validate(item: RollingSortationReview): MutableList<String> {
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

    fun requireValid(item: RollingSortationReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingSortationReview>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingSortationReviewValidator(): RollingSortationReviewValidator {
    return RollingSortationReviewValidator(
        mutableListOf<RollingSortationReviewRule>(
            RollingSortationReviewCodePrefixRule("Q"),
            RollingSortationReviewBatchSizeRangeRule(80, 80),
            RollingSortationReviewLabelLengthRule(255),
            RollingSortationReviewSummaryLengthRule(80),
        ),
    )
}
