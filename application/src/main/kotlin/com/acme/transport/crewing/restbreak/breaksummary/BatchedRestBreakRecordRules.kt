package com.acme.transport.crewing.restbreak.breaksummary

/** A rule a batched rest break record must pass. Check returns its problems, or none. */
interface BatchedRestBreakRecordRule {
    fun check(item: BatchedRestBreakRecord): MutableList<String>
}

data class BatchedRestBreakRecordCodePrefixRule(var prefix: String) : BatchedRestBreakRecordRule {
    override fun check(item: BatchedRestBreakRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRestBreakRecordRequiredRouteCodeRule(var message: String) : BatchedRestBreakRecordRule {
    override fun check(item: BatchedRestBreakRecord): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedRestBreakRecordExternalRefLengthRule(var maxLength: Int) : BatchedRestBreakRecordRule {
    override fun check(item: BatchedRestBreakRecord): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRestBreakRecordWindowDaysRangeRule(var min: Int, var max: Int) : BatchedRestBreakRecordRule {
    override fun check(item: BatchedRestBreakRecord): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRestBreakRecordUpdatedAtRangeRule(var min: Int, var max: Int) : BatchedRestBreakRecordRule {
    override fun check(item: BatchedRestBreakRecord): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched rest break record. */
data class BatchedRestBreakRecordValidator(var rules: MutableList<BatchedRestBreakRecordRule>) {
    fun validate(item: BatchedRestBreakRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedRestBreakRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedRestBreakRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedRestBreakRecordValidator(): BatchedRestBreakRecordValidator {
    return BatchedRestBreakRecordValidator(
        mutableListOf<BatchedRestBreakRecordRule>(
            BatchedRestBreakRecordCodePrefixRule("REF"),
            BatchedRestBreakRecordRequiredRouteCodeRule("batched rest break record needs a value"),
            BatchedRestBreakRecordExternalRefLengthRule(80),
            BatchedRestBreakRecordWindowDaysRangeRule(1000, 3600),
            BatchedRestBreakRecordUpdatedAtRangeRule(80, 3600),
        ),
    )
}
