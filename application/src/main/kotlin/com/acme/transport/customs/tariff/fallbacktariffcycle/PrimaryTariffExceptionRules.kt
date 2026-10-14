package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.kit.text.normalize

/** A rule a primary tariff exception must pass. Check returns its problems, or none. */
interface PrimaryTariffExceptionRule {
    fun check(item: PrimaryTariffException): MutableList<String>
}

data class PrimaryTariffExceptionRequiredTitleRule(var message: String) : PrimaryTariffExceptionRule {
    override fun check(item: PrimaryTariffException): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTariffExceptionTitleLengthRule(var maxLength: Int) : PrimaryTariffExceptionRule {
    override fun check(item: PrimaryTariffException): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTariffExceptionAllowedTitleRule(
    var allowed: MutableList<String>,
) : PrimaryTariffExceptionRule {
    override fun check(item: PrimaryTariffException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTariffExceptionRequiredCodeRule(var message: String) : PrimaryTariffExceptionRule {
    override fun check(item: PrimaryTariffException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTariffExceptionDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryTariffExceptionRule {
    override fun check(item: PrimaryTariffException): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTariffExceptionCodePrefixRule(var prefix: String) : PrimaryTariffExceptionRule {
    override fun check(item: PrimaryTariffException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary tariff exception. */
data class PrimaryTariffExceptionValidator(var rules: MutableList<PrimaryTariffExceptionRule>) {
    fun validate(item: PrimaryTariffException): MutableList<String> {
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

    fun requireValid(item: PrimaryTariffException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTariffException>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTariffExceptionValidator(): PrimaryTariffExceptionValidator {
    return PrimaryTariffExceptionValidator(
        mutableListOf<PrimaryTariffExceptionRule>(
            PrimaryTariffExceptionRequiredTitleRule("primary tariff exception needs this field"),
            PrimaryTariffExceptionTitleLengthRule(255),
            PrimaryTariffExceptionAllowedTitleRule(mutableListOf<String>("fallback", "seasonal")),
            PrimaryTariffExceptionRequiredCodeRule("primary tariff exception needs a value"),
            PrimaryTariffExceptionDistanceKmRangeRule(3.0, 3160.9),
            PrimaryTariffExceptionCodePrefixRule("X"),
        ),
    )
}
