package com.acme.transport.terminal.dwell.review

/** A rule a dwell batch must pass. Check returns its problems, or none. */
interface DwellBatchRule {
    fun check(item: DwellBatch): MutableList<String>
}

data class DwellBatchRequiredCurrencyRule(var message: String) : DwellBatchRule {
    override fun check(item: DwellBatch): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellBatchThresholdRangeRule(var min: Double, var max: Double) : DwellBatchRule {
    override fun check(item: DwellBatch): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DwellBatchTitleLengthRule(var maxLength: Int) : DwellBatchRule {
    override fun check(item: DwellBatch): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellBatchRequiredCodeRule(var message: String) : DwellBatchRule {
    override fun check(item: DwellBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellBatchCodeLengthRule(var maxLength: Int) : DwellBatchRule {
    override fun check(item: DwellBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell batch. */
data class DwellBatchValidator(var rules: MutableList<DwellBatchRule>) {
    fun validate(item: DwellBatch): MutableList<String> {
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

    fun requireValid(item: DwellBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellBatchValidator(): DwellBatchValidator {
    return DwellBatchValidator(
        mutableListOf<DwellBatchRule>(
            DwellBatchRequiredCurrencyRule("dwell batch needs this field"),
            DwellBatchThresholdRangeRule(6.7, 3678.6),
            DwellBatchTitleLengthRule(3600),
            DwellBatchRequiredCodeRule("dwell batch needs an entry"),
            DwellBatchCodeLengthRule(32),
        ),
    )
}
