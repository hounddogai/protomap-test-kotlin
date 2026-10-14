package com.acme.transport.terminal.crossdock.entry

/** A rule a batched cross dock entry must pass. Check returns its problems, or none. */
interface BatchedCrossDockEntryRule {
    fun check(item: BatchedCrossDockEntry): MutableList<String>
}

data class BatchedCrossDockEntryRequiredNameRule(var message: String) : BatchedCrossDockEntryRule {
    override fun check(item: BatchedCrossDockEntry): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedCrossDockEntryCodeLengthRule(var maxLength: Int) : BatchedCrossDockEntryRule {
    override fun check(item: BatchedCrossDockEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCrossDockEntryLabelLengthRule(var maxLength: Int) : BatchedCrossDockEntryRule {
    override fun check(item: BatchedCrossDockEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCrossDockEntryScoreRangeRule(var min: Double, var max: Double) : BatchedCrossDockEntryRule {
    override fun check(item: BatchedCrossDockEntry): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class BatchedCrossDockEntryRequiredCodeRule(var message: String) : BatchedCrossDockEntryRule {
    override fun check(item: BatchedCrossDockEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched cross dock entry. */
data class BatchedCrossDockEntryValidator(var rules: MutableList<BatchedCrossDockEntryRule>) {
    fun validate(item: BatchedCrossDockEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedCrossDockEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedCrossDockEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedCrossDockEntryValidator(): BatchedCrossDockEntryValidator {
    return BatchedCrossDockEntryValidator(
        mutableListOf<BatchedCrossDockEntryRule>(
            BatchedCrossDockEntryRequiredNameRule("batched cross dock entry needs a value"),
            BatchedCrossDockEntryCodeLengthRule(3600),
            BatchedCrossDockEntryLabelLengthRule(3600),
            BatchedCrossDockEntryScoreRangeRule(1.8, 1048.8),
            BatchedCrossDockEntryRequiredCodeRule("batched cross dock entry needs a value"),
        ),
    )
}
