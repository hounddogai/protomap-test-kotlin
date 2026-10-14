package com.acme.transport.network.servicearea.digest

/** A rule a strict service area batch must pass. Check returns its problems, or none. */
interface StrictServiceAreaBatchRule {
    fun check(item: StrictServiceAreaBatch): MutableList<String>
}

data class StrictServiceAreaBatchCurrencyLengthRule(var maxLength: Int) : StrictServiceAreaBatchRule {
    override fun check(item: StrictServiceAreaBatch): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictServiceAreaBatchRatioRangeRule(var min: Double, var max: Double) : StrictServiceAreaBatchRule {
    override fun check(item: StrictServiceAreaBatch): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class StrictServiceAreaBatchRequiredCurrencyRule(var message: String) : StrictServiceAreaBatchRule {
    override fun check(item: StrictServiceAreaBatch): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictServiceAreaBatchRequiredLabelRule(var message: String) : StrictServiceAreaBatchRule {
    override fun check(item: StrictServiceAreaBatch): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict service area batch. */
data class StrictServiceAreaBatchValidator(var rules: MutableList<StrictServiceAreaBatchRule>) {
    fun validate(item: StrictServiceAreaBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictServiceAreaBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictServiceAreaBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictServiceAreaBatchValidator(): StrictServiceAreaBatchValidator {
    return StrictServiceAreaBatchValidator(
        mutableListOf<StrictServiceAreaBatchRule>(
            StrictServiceAreaBatchCurrencyLengthRule(64),
            StrictServiceAreaBatchRatioRangeRule(6.9, 1856.9),
            StrictServiceAreaBatchRequiredCurrencyRule("strict service area batch needs an entry"),
            StrictServiceAreaBatchRequiredLabelRule("strict service area batch needs a value"),
        ),
    )
}
