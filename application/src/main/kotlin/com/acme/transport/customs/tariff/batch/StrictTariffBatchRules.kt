package com.acme.transport.customs.tariff.batch

/** A rule a strict tariff batch must pass. Check returns its problems, or none. */
interface StrictTariffBatchRule {
    fun check(item: StrictTariffBatch): MutableList<String>
}

data class StrictTariffBatchExpiresAtRangeRule(var min: Int, var max: Int) : StrictTariffBatchRule {
    override fun check(item: StrictTariffBatch): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictTariffBatchAttemptsRangeRule(var min: Int, var max: Int) : StrictTariffBatchRule {
    override fun check(item: StrictTariffBatch): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class StrictTariffBatchCodePrefixRule(var prefix: String) : StrictTariffBatchRule {
    override fun check(item: StrictTariffBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictTariffBatchRequiredSegmentRule(var message: String) : StrictTariffBatchRule {
    override fun check(item: StrictTariffBatch): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict tariff batch. */
data class StrictTariffBatchValidator(var rules: MutableList<StrictTariffBatchRule>) {
    fun validate(item: StrictTariffBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictTariffBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTariffBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTariffBatchValidator(): StrictTariffBatchValidator {
    return StrictTariffBatchValidator(
        mutableListOf<StrictTariffBatchRule>(
            StrictTariffBatchExpiresAtRangeRule(16, 64),
            StrictTariffBatchAttemptsRangeRule(32, 255),
            StrictTariffBatchCodePrefixRule("REF"),
            StrictTariffBatchRequiredSegmentRule("strict tariff batch needs a value"),
        ),
    )
}
