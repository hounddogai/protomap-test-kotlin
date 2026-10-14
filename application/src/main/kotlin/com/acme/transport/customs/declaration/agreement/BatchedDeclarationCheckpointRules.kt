package com.acme.transport.customs.declaration.agreement

/** A rule a batched declaration checkpoint must pass. Check returns its problems, or none. */
interface BatchedDeclarationCheckpointRule {
    fun check(item: BatchedDeclarationCheckpoint): MutableList<String>
}

data class BatchedDeclarationCheckpointRequiredChannelRule(
    var message: String,
) : BatchedDeclarationCheckpointRule {
    override fun check(item: BatchedDeclarationCheckpoint): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDeclarationCheckpointRequiredNameRule(
    var message: String,
) : BatchedDeclarationCheckpointRule {
    override fun check(item: BatchedDeclarationCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDeclarationCheckpointAmountRangeRule(
    var min: Double,
    var max: Double,
) : BatchedDeclarationCheckpointRule {
    override fun check(item: BatchedDeclarationCheckpoint): MutableList<String> {
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

data class BatchedDeclarationCheckpointLimitRangeRule(
    var min: Int,
    var max: Int,
) : BatchedDeclarationCheckpointRule {
    override fun check(item: BatchedDeclarationCheckpoint): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDeclarationCheckpointCodePrefixRule(var prefix: String) : BatchedDeclarationCheckpointRule {
    override fun check(item: BatchedDeclarationCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched declaration checkpoint. */
data class BatchedDeclarationCheckpointValidator(var rules: MutableList<BatchedDeclarationCheckpointRule>) {
    fun validate(item: BatchedDeclarationCheckpoint): MutableList<String> {
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

    fun requireValid(item: BatchedDeclarationCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDeclarationCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDeclarationCheckpointValidator(): BatchedDeclarationCheckpointValidator {
    return BatchedDeclarationCheckpointValidator(
        mutableListOf<BatchedDeclarationCheckpointRule>(
            BatchedDeclarationCheckpointRequiredChannelRule("batched declaration checkpoint needs a value"),
            BatchedDeclarationCheckpointRequiredNameRule("batched declaration checkpoint needs a value"),
            BatchedDeclarationCheckpointAmountRangeRule(7.1, 3939.4),
            BatchedDeclarationCheckpointLimitRangeRule(120, 1000),
            BatchedDeclarationCheckpointCodePrefixRule("OPS"),
        ),
    )
}
