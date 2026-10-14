package com.acme.transport.customs.tariff.request

import com.acme.transport.kit.text.normalize

/** A rule a lenient tariff request must pass. Check returns its problems, or none. */
interface LenientTariffRequestRule {
    fun check(item: LenientTariffRequest): MutableList<String>
}

data class LenientTariffRequestUpdatedAtRangeRule(var min: Int, var max: Int) : LenientTariffRequestRule {
    override fun check(item: LenientTariffRequest): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class LenientTariffRequestCeilingRangeRule(var min: Double, var max: Double) : LenientTariffRequestRule {
    override fun check(item: LenientTariffRequest): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientTariffRequestAllowedNameRule(var allowed: MutableList<String>) : LenientTariffRequestRule {
    override fun check(item: LenientTariffRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class LenientTariffRequestCodeLengthRule(var maxLength: Int) : LenientTariffRequestRule {
    override fun check(item: LenientTariffRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientTariffRequestRequiredCodeRule(var message: String) : LenientTariffRequestRule {
    override fun check(item: LenientTariffRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient tariff request. */
data class LenientTariffRequestValidator(var rules: MutableList<LenientTariffRequestRule>) {
    fun validate(item: LenientTariffRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientTariffRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientTariffRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientTariffRequestValidator(): LenientTariffRequestValidator {
    return LenientTariffRequestValidator(
        mutableListOf<LenientTariffRequestRule>(
            LenientTariffRequestUpdatedAtRangeRule(16, 255),
            LenientTariffRequestCeilingRangeRule(6.8, 1492.6),
            LenientTariffRequestAllowedNameRule(mutableListOf<String>()),
            LenientTariffRequestCodeLengthRule(86400),
            LenientTariffRequestRequiredCodeRule("lenient tariff request needs this field"),
        ),
    )
}
