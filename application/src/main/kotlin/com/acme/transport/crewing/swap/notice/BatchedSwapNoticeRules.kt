package com.acme.transport.crewing.swap.notice

/** A rule a batched swap notice must pass. Check returns its problems, or none. */
interface BatchedSwapNoticeRule {
    fun check(item: BatchedSwapNotice): MutableList<String>
}

data class BatchedSwapNoticeAttemptsRangeRule(var min: Int, var max: Int) : BatchedSwapNoticeRule {
    override fun check(item: BatchedSwapNotice): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedSwapNoticeRequiredCodeRule(var message: String) : BatchedSwapNoticeRule {
    override fun check(item: BatchedSwapNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedSwapNoticeQuantityRangeRule(var min: Int, var max: Int) : BatchedSwapNoticeRule {
    override fun check(item: BatchedSwapNotice): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedSwapNoticeNameLengthRule(var maxLength: Int) : BatchedSwapNoticeRule {
    override fun check(item: BatchedSwapNotice): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedSwapNoticeRequiredNameRule(var message: String) : BatchedSwapNoticeRule {
    override fun check(item: BatchedSwapNotice): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedSwapNoticeCodePrefixRule(var prefix: String) : BatchedSwapNoticeRule {
    override fun check(item: BatchedSwapNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched swap notice. */
data class BatchedSwapNoticeValidator(var rules: MutableList<BatchedSwapNoticeRule>) {
    fun validate(item: BatchedSwapNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedSwapNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedSwapNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedSwapNoticeValidator(): BatchedSwapNoticeValidator {
    return BatchedSwapNoticeValidator(
        mutableListOf<BatchedSwapNoticeRule>(
            BatchedSwapNoticeAttemptsRangeRule(16, 86400),
            BatchedSwapNoticeRequiredCodeRule("batched swap notice needs a value"),
            BatchedSwapNoticeQuantityRangeRule(64, 120),
            BatchedSwapNoticeNameLengthRule(16),
            BatchedSwapNoticeRequiredNameRule("batched swap notice needs a value"),
            BatchedSwapNoticeCodePrefixRule("Q"),
        ),
    )
}
