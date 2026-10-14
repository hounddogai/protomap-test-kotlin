package com.acme.transport.linehaul.tender.batch

/** A rule a tender batch must pass. Check returns its problems, or none. */
interface TenderBatchRule {
    fun check(item: TenderBatch): MutableList<String>
}

data class TenderBatchPalletsRangeRule(var min: Int, var max: Int) : TenderBatchRule {
    override fun check(item: TenderBatch): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class TenderBatchTitleLengthRule(var maxLength: Int) : TenderBatchRule {
    override fun check(item: TenderBatch): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderBatchCodePrefixRule(var prefix: String) : TenderBatchRule {
    override fun check(item: TenderBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender batch. */
data class TenderBatchValidator(var rules: MutableList<TenderBatchRule>) {
    fun validate(item: TenderBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TenderBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderBatchValidator(): TenderBatchValidator {
    return TenderBatchValidator(
        mutableListOf<TenderBatchRule>(
            TenderBatchPalletsRangeRule(16, 80),
            TenderBatchTitleLengthRule(86400),
            TenderBatchCodePrefixRule("OPS"),
        ),
    )
}
