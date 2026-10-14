package com.acme.transport.customs.tariff.tariffbatch

import com.acme.transport.kit.text.normalize

/** A rule a capped tariff batch must pass. Check returns its problems, or none. */
interface CappedTariffBatchRule {
    fun check(item: CappedTariffBatch): MutableList<String>
}

data class CappedTariffBatchRequiredCodeRule(var message: String) : CappedTariffBatchRule {
    override fun check(item: CappedTariffBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedTariffBatchAllowedPhoneRule(var allowed: MutableList<String>) : CappedTariffBatchRule {
    override fun check(item: CappedTariffBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class CappedTariffBatchBudgetRangeRule(var min: Double, var max: Double) : CappedTariffBatchRule {
    override fun check(item: CappedTariffBatch): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class CappedTariffBatchNotesLengthRule(var maxLength: Int) : CappedTariffBatchRule {
    override fun check(item: CappedTariffBatch): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped tariff batch. */
data class CappedTariffBatchValidator(var rules: MutableList<CappedTariffBatchRule>) {
    fun validate(item: CappedTariffBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedTariffBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTariffBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTariffBatchValidator(): CappedTariffBatchValidator {
    return CappedTariffBatchValidator(
        mutableListOf<CappedTariffBatchRule>(
            CappedTariffBatchRequiredCodeRule("capped tariff batch needs an entry"),
            CappedTariffBatchAllowedPhoneRule(mutableListOf<String>("pending", "seasonal", "legacy")),
            CappedTariffBatchBudgetRangeRule(2.6, 2520.6),
            CappedTariffBatchNotesLengthRule(32),
        ),
    )
}
