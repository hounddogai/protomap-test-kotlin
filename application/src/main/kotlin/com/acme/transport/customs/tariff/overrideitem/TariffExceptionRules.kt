package com.acme.transport.customs.tariff.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a tariff exception must pass. Check returns its problems, or none. */
interface TariffExceptionRule {
    fun check(item: TariffException): MutableList<String>
}

data class TariffExceptionAllowedCurrencyRule(var allowed: MutableList<String>) : TariffExceptionRule {
    override fun check(item: TariffException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.currency))) {
            return mutableListOf<String>("currency ${item.currency} is missing")
        }
        return mutableListOf<String>()
    }
}

data class TariffExceptionBatchSizeRangeRule(var min: Int, var max: Int) : TariffExceptionRule {
    override fun check(item: TariffException): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TariffExceptionRequiredCodeRule(var message: String) : TariffExceptionRule {
    override fun check(item: TariffException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffExceptionCurrencyLengthRule(var maxLength: Int) : TariffExceptionRule {
    override fun check(item: TariffException): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffExceptionCodeLengthRule(var maxLength: Int) : TariffExceptionRule {
    override fun check(item: TariffException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff exception. */
data class TariffExceptionValidator(var rules: MutableList<TariffExceptionRule>) {
    fun validate(item: TariffException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TariffException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffException>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffExceptionValidator(): TariffExceptionValidator {
    return TariffExceptionValidator(
        mutableListOf<TariffExceptionRule>(
            TariffExceptionAllowedCurrencyRule(mutableListOf<String>("adaptive", "default", "batched")),
            TariffExceptionBatchSizeRangeRule(1000, 3600),
            TariffExceptionRequiredCodeRule("tariff exception needs this field"),
            TariffExceptionCurrencyLengthRule(1000),
            TariffExceptionCodeLengthRule(3600),
        ),
    )
}
