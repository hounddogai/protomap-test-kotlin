package com.acme.transport.lastmile.redelivery.threshold

/** A rule a tiered redelivery checkpoint must pass. Check returns its problems, or none. */
interface TieredRedeliveryCheckpointRule {
    fun check(item: TieredRedeliveryCheckpoint): MutableList<String>
}

data class TieredRedeliveryCheckpointCategoryLengthRule(var maxLength: Int) : TieredRedeliveryCheckpointRule {
    override fun check(item: TieredRedeliveryCheckpoint): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredRedeliveryCheckpointDescriptionLengthRule(
    var maxLength: Int,
) : TieredRedeliveryCheckpointRule {
    override fun check(item: TieredRedeliveryCheckpoint): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredRedeliveryCheckpointCodePrefixRule(var prefix: String) : TieredRedeliveryCheckpointRule {
    override fun check(item: TieredRedeliveryCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered redelivery checkpoint. */
data class TieredRedeliveryCheckpointValidator(var rules: MutableList<TieredRedeliveryCheckpointRule>) {
    fun validate(item: TieredRedeliveryCheckpoint): MutableList<String> {
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

    fun requireValid(item: TieredRedeliveryCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredRedeliveryCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredRedeliveryCheckpointValidator(): TieredRedeliveryCheckpointValidator {
    return TieredRedeliveryCheckpointValidator(
        mutableListOf<TieredRedeliveryCheckpointRule>(
            TieredRedeliveryCheckpointCategoryLengthRule(64),
            TieredRedeliveryCheckpointDescriptionLengthRule(64),
            TieredRedeliveryCheckpointCodePrefixRule("X"),
        ),
    )
}
