package com.acme.transport.customs.tariff.tariffcycle

/** A rule a tariff batch must pass. Check returns its problems, or none. */
interface TariffBatchRule {
    fun check(item: TariffBatch): MutableList<String>
}

data class TariffBatchAttemptsRangeRule(var min: Int, var max: Int) : TariffBatchRule {
    override fun check(item: TariffBatch): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TariffBatchCodeLengthRule(var maxLength: Int) : TariffBatchRule {
    override fun check(item: TariffBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffBatchQuantityRangeRule(var min: Int, var max: Int) : TariffBatchRule {
    override fun check(item: TariffBatch): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff batch. */
data class TariffBatchValidator(var rules: MutableList<TariffBatchRule>) {
    fun validate(item: TariffBatch): MutableList<String> {
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

    fun requireValid(item: TariffBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffBatchValidator(): TariffBatchValidator {
    return TariffBatchValidator(
        mutableListOf<TariffBatchRule>(
            TariffBatchAttemptsRangeRule(32, 86400),
            TariffBatchCodeLengthRule(120),
            TariffBatchQuantityRangeRule(16, 80),
        ),
    )
}
