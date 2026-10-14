package com.acme.transport.terminal.sortation.bundle

/** A rule a strict sortation batch must pass. Check returns its problems, or none. */
interface StrictSortationBatchRule {
    fun check(item: StrictSortationBatch): MutableList<String>
}

data class StrictSortationBatchRequiredExternalRefRule(var message: String) : StrictSortationBatchRule {
    override fun check(item: StrictSortationBatch): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictSortationBatchCodeLengthRule(var maxLength: Int) : StrictSortationBatchRule {
    override fun check(item: StrictSortationBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictSortationBatchScoreRangeRule(var min: Double, var max: Double) : StrictSortationBatchRule {
    override fun check(item: StrictSortationBatch): MutableList<String> {
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

data class StrictSortationBatchTrailerIdLengthRule(var maxLength: Int) : StrictSortationBatchRule {
    override fun check(item: StrictSortationBatch): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictSortationBatchCodePrefixRule(var prefix: String) : StrictSortationBatchRule {
    override fun check(item: StrictSortationBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictSortationBatchUpdatedAtRangeRule(var min: Int, var max: Int) : StrictSortationBatchRule {
    override fun check(item: StrictSortationBatch): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a strict sortation batch. */
data class StrictSortationBatchValidator(var rules: MutableList<StrictSortationBatchRule>) {
    fun validate(item: StrictSortationBatch): MutableList<String> {
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

    fun requireValid(item: StrictSortationBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictSortationBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictSortationBatchValidator(): StrictSortationBatchValidator {
    return StrictSortationBatchValidator(
        mutableListOf<StrictSortationBatchRule>(
            StrictSortationBatchRequiredExternalRefRule("strict sortation batch needs an entry"),
            StrictSortationBatchCodeLengthRule(120),
            StrictSortationBatchScoreRangeRule(6.2, 4783.2),
            StrictSortationBatchTrailerIdLengthRule(64),
            StrictSortationBatchCodePrefixRule("OPS"),
            StrictSortationBatchUpdatedAtRangeRule(32, 86400),
        ),
    )
}
