package com.acme.transport.terminal.dwell.ledger

/** A rule a capped dwell batch must pass. Check returns its problems, or none. */
interface CappedDwellBatchRule {
    fun check(item: CappedDwellBatch): MutableList<String>
}

data class CappedDwellBatchRequiredReviewerRule(var message: String) : CappedDwellBatchRule {
    override fun check(item: CappedDwellBatch): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDwellBatchCodePrefixRule(var prefix: String) : CappedDwellBatchRule {
    override fun check(item: CappedDwellBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedDwellBatchNameLengthRule(var maxLength: Int) : CappedDwellBatchRule {
    override fun check(item: CappedDwellBatch): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedDwellBatchFuelLitersRangeRule(var min: Double, var max: Double) : CappedDwellBatchRule {
    override fun check(item: CappedDwellBatch): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class CappedDwellBatchBatchSizeRangeRule(var min: Int, var max: Int) : CappedDwellBatchRule {
    override fun check(item: CappedDwellBatch): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped dwell batch. */
data class CappedDwellBatchValidator(var rules: MutableList<CappedDwellBatchRule>) {
    fun validate(item: CappedDwellBatch): MutableList<String> {
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

    fun requireValid(item: CappedDwellBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedDwellBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedDwellBatchValidator(): CappedDwellBatchValidator {
    return CappedDwellBatchValidator(
        mutableListOf<CappedDwellBatchRule>(
            CappedDwellBatchRequiredReviewerRule("capped dwell batch needs an entry"),
            CappedDwellBatchCodePrefixRule("OPS"),
            CappedDwellBatchNameLengthRule(86400),
            CappedDwellBatchFuelLitersRangeRule(5.6, 4942.5),
            CappedDwellBatchBatchSizeRangeRule(80, 255),
        ),
    )
}
