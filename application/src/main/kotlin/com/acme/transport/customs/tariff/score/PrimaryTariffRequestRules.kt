package com.acme.transport.customs.tariff.score

import com.acme.transport.kit.text.normalize

/** A rule a primary tariff request must pass. Check returns its problems, or none. */
interface PrimaryTariffRequestRule {
    fun check(item: PrimaryTariffRequest): MutableList<String>
}

data class PrimaryTariffRequestStartsAtRangeRule(var min: Int, var max: Int) : PrimaryTariffRequestRule {
    override fun check(item: PrimaryTariffRequest): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class PrimaryTariffRequestPriorityRangeRule(var min: Int, var max: Int) : PrimaryTariffRequestRule {
    override fun check(item: PrimaryTariffRequest): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class PrimaryTariffRequestWeightRangeRule(var min: Double, var max: Double) : PrimaryTariffRequestRule {
    override fun check(item: PrimaryTariffRequest): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class PrimaryTariffRequestCodeLengthRule(var maxLength: Int) : PrimaryTariffRequestRule {
    override fun check(item: PrimaryTariffRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTariffRequestLocaleLengthRule(var maxLength: Int) : PrimaryTariffRequestRule {
    override fun check(item: PrimaryTariffRequest): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTariffRequestAllowedCodeRule(var allowed: MutableList<String>) : PrimaryTariffRequestRule {
    override fun check(item: PrimaryTariffRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary tariff request. */
data class PrimaryTariffRequestValidator(var rules: MutableList<PrimaryTariffRequestRule>) {
    fun validate(item: PrimaryTariffRequest): MutableList<String> {
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

    fun requireValid(item: PrimaryTariffRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTariffRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTariffRequestValidator(): PrimaryTariffRequestValidator {
    return PrimaryTariffRequestValidator(
        mutableListOf<PrimaryTariffRequestRule>(
            PrimaryTariffRequestStartsAtRangeRule(120, 3600),
            PrimaryTariffRequestPriorityRangeRule(1000, 86400),
            PrimaryTariffRequestWeightRangeRule(2.1, 4460.5),
            PrimaryTariffRequestCodeLengthRule(255),
            PrimaryTariffRequestLocaleLengthRule(86400),
            PrimaryTariffRequestAllowedCodeRule(mutableListOf<String>("regional")),
        ),
    )
}
