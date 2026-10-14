package com.acme.transport.crewing.restbreak.adjustment

/** A rule a batched rest break review must pass. Check returns its problems, or none. */
interface BatchedRestBreakReviewRule {
    fun check(item: BatchedRestBreakReview): MutableList<String>
}

data class BatchedRestBreakReviewLabelLengthRule(var maxLength: Int) : BatchedRestBreakReviewRule {
    override fun check(item: BatchedRestBreakReview): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRestBreakReviewTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : BatchedRestBreakReviewRule {
    override fun check(item: BatchedRestBreakReview): MutableList<String> {
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

data class BatchedRestBreakReviewRequiredLabelRule(var message: String) : BatchedRestBreakReviewRule {
    override fun check(item: BatchedRestBreakReview): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedRestBreakReviewTimezoneLengthRule(var maxLength: Int) : BatchedRestBreakReviewRule {
    override fun check(item: BatchedRestBreakReview): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched rest break review. */
data class BatchedRestBreakReviewValidator(var rules: MutableList<BatchedRestBreakReviewRule>) {
    fun validate(item: BatchedRestBreakReview): MutableList<String> {
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

    fun requireValid(item: BatchedRestBreakReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedRestBreakReview>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedRestBreakReviewValidator(): BatchedRestBreakReviewValidator {
    return BatchedRestBreakReviewValidator(
        mutableListOf<BatchedRestBreakReviewRule>(
            BatchedRestBreakReviewLabelLengthRule(3600),
            BatchedRestBreakReviewTagsLimitRule(80, mutableListOf<String>("default", "legacy", "default")),
            BatchedRestBreakReviewRequiredLabelRule("batched rest break review needs a value"),
            BatchedRestBreakReviewTimezoneLengthRule(120),
        ),
    )
}
