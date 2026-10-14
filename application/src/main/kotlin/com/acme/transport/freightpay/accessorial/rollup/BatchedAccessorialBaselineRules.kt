package com.acme.transport.freightpay.accessorial.rollup

/** A rule a batched accessorial baseline must pass. Check returns its problems, or none. */
interface BatchedAccessorialBaselineRule {
    fun check(item: BatchedAccessorialBaseline): MutableList<String>
}

data class BatchedAccessorialBaselineTrailerIdLengthRule(var maxLength: Int) : BatchedAccessorialBaselineRule {
    override fun check(item: BatchedAccessorialBaseline): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAccessorialBaselineRequiredTrailerIdRule(
    var message: String,
) : BatchedAccessorialBaselineRule {
    override fun check(item: BatchedAccessorialBaseline): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAccessorialBaselineRequiredCategoryRule(
    var message: String,
) : BatchedAccessorialBaselineRule {
    override fun check(item: BatchedAccessorialBaseline): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAccessorialBaselineCategoryLengthRule(var maxLength: Int) : BatchedAccessorialBaselineRule {
    override fun check(item: BatchedAccessorialBaseline): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched accessorial baseline. */
data class BatchedAccessorialBaselineValidator(var rules: MutableList<BatchedAccessorialBaselineRule>) {
    fun validate(item: BatchedAccessorialBaseline): MutableList<String> {
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

    fun requireValid(item: BatchedAccessorialBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAccessorialBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAccessorialBaselineValidator(): BatchedAccessorialBaselineValidator {
    return BatchedAccessorialBaselineValidator(
        mutableListOf<BatchedAccessorialBaselineRule>(
            BatchedAccessorialBaselineTrailerIdLengthRule(16),
            BatchedAccessorialBaselineRequiredTrailerIdRule("batched accessorial baseline needs a value"),
            BatchedAccessorialBaselineRequiredCategoryRule("batched accessorial baseline needs an entry"),
            BatchedAccessorialBaselineCategoryLengthRule(120),
        ),
    )
}
