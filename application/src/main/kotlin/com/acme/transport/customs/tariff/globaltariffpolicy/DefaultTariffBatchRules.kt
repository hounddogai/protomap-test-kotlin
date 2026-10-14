package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.kit.text.normalize

/** A rule a default tariff batch must pass. Check returns its problems, or none. */
interface DefaultTariffBatchRule {
    fun check(item: DefaultTariffBatch): MutableList<String>
}

data class DefaultTariffBatchStartsAtRangeRule(var min: Int, var max: Int) : DefaultTariffBatchRule {
    override fun check(item: DefaultTariffBatch): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTariffBatchRegionLengthRule(var maxLength: Int) : DefaultTariffBatchRule {
    override fun check(item: DefaultTariffBatch): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTariffBatchAllowedTrailerIdRule(var allowed: MutableList<String>) : DefaultTariffBatchRule {
    override fun check(item: DefaultTariffBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTariffBatchRequiredCodeRule(var message: String) : DefaultTariffBatchRule {
    override fun check(item: DefaultTariffBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default tariff batch. */
data class DefaultTariffBatchValidator(var rules: MutableList<DefaultTariffBatchRule>) {
    fun validate(item: DefaultTariffBatch): MutableList<String> {
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

    fun requireValid(item: DefaultTariffBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTariffBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTariffBatchValidator(): DefaultTariffBatchValidator {
    return DefaultTariffBatchValidator(
        mutableListOf<DefaultTariffBatchRule>(
            DefaultTariffBatchStartsAtRangeRule(80, 86400),
            DefaultTariffBatchRegionLengthRule(3600),
            DefaultTariffBatchAllowedTrailerIdRule(mutableListOf<String>("seasonal", "seasonal")),
            DefaultTariffBatchRequiredCodeRule("default tariff batch needs this field"),
        ),
    )
}
