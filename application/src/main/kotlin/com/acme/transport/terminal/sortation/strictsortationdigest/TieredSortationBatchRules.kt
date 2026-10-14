package com.acme.transport.terminal.sortation.strictsortationdigest

/** A rule a tiered sortation batch must pass. Check returns its problems, or none. */
interface TieredSortationBatchRule {
    fun check(item: TieredSortationBatch): MutableList<String>
}

data class TieredSortationBatchShiftCodeLengthRule(var maxLength: Int) : TieredSortationBatchRule {
    override fun check(item: TieredSortationBatch): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationBatchRequiredNameRule(var message: String) : TieredSortationBatchRule {
    override fun check(item: TieredSortationBatch): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationBatchRequiredShiftCodeRule(var message: String) : TieredSortationBatchRule {
    override fun check(item: TieredSortationBatch): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationBatchEndsAtRangeRule(var min: Int, var max: Int) : TieredSortationBatchRule {
    override fun check(item: TieredSortationBatch): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tiered sortation batch. */
data class TieredSortationBatchValidator(var rules: MutableList<TieredSortationBatchRule>) {
    fun validate(item: TieredSortationBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredSortationBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredSortationBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredSortationBatchValidator(): TieredSortationBatchValidator {
    return TieredSortationBatchValidator(
        mutableListOf<TieredSortationBatchRule>(
            TieredSortationBatchShiftCodeLengthRule(32),
            TieredSortationBatchRequiredNameRule("tiered sortation batch needs a value"),
            TieredSortationBatchRequiredShiftCodeRule("tiered sortation batch needs a value"),
            TieredSortationBatchEndsAtRangeRule(32, 1000),
        ),
    )
}
