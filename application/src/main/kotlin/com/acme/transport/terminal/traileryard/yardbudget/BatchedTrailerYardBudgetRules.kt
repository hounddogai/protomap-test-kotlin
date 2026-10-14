package com.acme.transport.terminal.traileryard.yardbudget

import com.acme.transport.kit.text.normalize

/** A rule a batched trailer yard budget must pass. Check returns its problems, or none. */
interface BatchedTrailerYardBudgetRule {
    fun check(item: BatchedTrailerYardBudget): MutableList<String>
}

data class BatchedTrailerYardBudgetRequiredCodeRule(var message: String) : BatchedTrailerYardBudgetRule {
    override fun check(item: BatchedTrailerYardBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerYardBudgetAllowedPhoneRule(
    var allowed: MutableList<String>,
) : BatchedTrailerYardBudgetRule {
    override fun check(item: BatchedTrailerYardBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerYardBudgetPhoneLengthRule(var maxLength: Int) : BatchedTrailerYardBudgetRule {
    override fun check(item: BatchedTrailerYardBudget): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerYardBudgetTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : BatchedTrailerYardBudgetRule {
    override fun check(item: BatchedTrailerYardBudget): MutableList<String> {
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

data class BatchedTrailerYardBudgetCodeLengthRule(var maxLength: Int) : BatchedTrailerYardBudgetRule {
    override fun check(item: BatchedTrailerYardBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched trailer yard budget. */
data class BatchedTrailerYardBudgetValidator(var rules: MutableList<BatchedTrailerYardBudgetRule>) {
    fun validate(item: BatchedTrailerYardBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedTrailerYardBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTrailerYardBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTrailerYardBudgetValidator(): BatchedTrailerYardBudgetValidator {
    return BatchedTrailerYardBudgetValidator(
        mutableListOf<BatchedTrailerYardBudgetRule>(
            BatchedTrailerYardBudgetRequiredCodeRule("batched trailer yard budget needs this field"),
            BatchedTrailerYardBudgetAllowedPhoneRule(mutableListOf<String>("pending", "fallback")),
            BatchedTrailerYardBudgetPhoneLengthRule(3600),
            BatchedTrailerYardBudgetTagsLimitRule(3600, mutableListOf<String>()),
            BatchedTrailerYardBudgetCodeLengthRule(1000),
        ),
    )
}
