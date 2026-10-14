package com.acme.transport.crewing.roster.budget

/** A rule a batched roster template must pass. Check returns its problems, or none. */
interface BatchedRosterTemplateRule {
    fun check(item: BatchedRosterTemplate): MutableList<String>
}

data class BatchedRosterTemplateCodePrefixRule(var prefix: String) : BatchedRosterTemplateRule {
    override fun check(item: BatchedRosterTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterTemplateAmountRangeRule(var min: Double, var max: Double) : BatchedRosterTemplateRule {
    override fun check(item: BatchedRosterTemplate): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class BatchedRosterTemplateCreatedAtRangeRule(var min: Int, var max: Int) : BatchedRosterTemplateRule {
    override fun check(item: BatchedRosterTemplate): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterTemplateCodeLengthRule(var maxLength: Int) : BatchedRosterTemplateRule {
    override fun check(item: BatchedRosterTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterTemplateNameLengthRule(var maxLength: Int) : BatchedRosterTemplateRule {
    override fun check(item: BatchedRosterTemplate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterTemplateRequiredNameRule(var message: String) : BatchedRosterTemplateRule {
    override fun check(item: BatchedRosterTemplate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched roster template. */
data class BatchedRosterTemplateValidator(var rules: MutableList<BatchedRosterTemplateRule>) {
    fun validate(item: BatchedRosterTemplate): MutableList<String> {
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

    fun requireValid(item: BatchedRosterTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedRosterTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedRosterTemplateValidator(): BatchedRosterTemplateValidator {
    return BatchedRosterTemplateValidator(
        mutableListOf<BatchedRosterTemplateRule>(
            BatchedRosterTemplateCodePrefixRule("X"),
            BatchedRosterTemplateAmountRangeRule(4.7, 4236.7),
            BatchedRosterTemplateCreatedAtRangeRule(80, 3600),
            BatchedRosterTemplateCodeLengthRule(64),
            BatchedRosterTemplateNameLengthRule(80),
            BatchedRosterTemplateRequiredNameRule("batched roster template needs an entry"),
        ),
    )
}
