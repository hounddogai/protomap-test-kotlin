package com.acme.transport.network.cutoff.scheduledcutoffclaim

/** A rule a legacy cutoff batch must pass. Check returns its problems, or none. */
interface LegacyCutoffBatchRule {
    fun check(item: LegacyCutoffBatch): MutableList<String>
}

data class LegacyCutoffBatchRequiredCurrencyRule(var message: String) : LegacyCutoffBatchRule {
    override fun check(item: LegacyCutoffBatch): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyCutoffBatchFloorValueRangeRule(var min: Double, var max: Double) : LegacyCutoffBatchRule {
    override fun check(item: LegacyCutoffBatch): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCutoffBatchCodeLengthRule(var maxLength: Int) : LegacyCutoffBatchRule {
    override fun check(item: LegacyCutoffBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy cutoff batch. */
data class LegacyCutoffBatchValidator(var rules: MutableList<LegacyCutoffBatchRule>) {
    fun validate(item: LegacyCutoffBatch): MutableList<String> {
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

    fun requireValid(item: LegacyCutoffBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyCutoffBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyCutoffBatchValidator(): LegacyCutoffBatchValidator {
    return LegacyCutoffBatchValidator(
        mutableListOf<LegacyCutoffBatchRule>(
            LegacyCutoffBatchRequiredCurrencyRule("legacy cutoff batch needs an entry"),
            LegacyCutoffBatchFloorValueRangeRule(7.4, 2056.6),
            LegacyCutoffBatchCodeLengthRule(32),
        ),
    )
}
