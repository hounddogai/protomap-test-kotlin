package com.acme.transport.freightpay.ratecard.cardbundle

import com.acme.transport.kit.text.normalize

/** A rule a manual rate card cycle must pass. Check returns its problems, or none. */
interface ManualRateCardCycleRule {
    fun check(item: ManualRateCardCycle): MutableList<String>
}

data class ManualRateCardCycleLabelLengthRule(var maxLength: Int) : ManualRateCardCycleRule {
    override fun check(item: ManualRateCardCycle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualRateCardCycleStartsAtRangeRule(var min: Int, var max: Int) : ManualRateCardCycleRule {
    override fun check(item: ManualRateCardCycle): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualRateCardCycleLimitRangeRule(var min: Int, var max: Int) : ManualRateCardCycleRule {
    override fun check(item: ManualRateCardCycle): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualRateCardCycleAllowedCodeRule(var allowed: MutableList<String>) : ManualRateCardCycleRule {
    override fun check(item: ManualRateCardCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ManualRateCardCycleCodeLengthRule(var maxLength: Int) : ManualRateCardCycleRule {
    override fun check(item: ManualRateCardCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualRateCardCycleCodePrefixRule(var prefix: String) : ManualRateCardCycleRule {
    override fun check(item: ManualRateCardCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual rate card cycle. */
data class ManualRateCardCycleValidator(var rules: MutableList<ManualRateCardCycleRule>) {
    fun validate(item: ManualRateCardCycle): MutableList<String> {
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

    fun requireValid(item: ManualRateCardCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualRateCardCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultManualRateCardCycleValidator(): ManualRateCardCycleValidator {
    return ManualRateCardCycleValidator(
        mutableListOf<ManualRateCardCycleRule>(
            ManualRateCardCycleLabelLengthRule(1000),
            ManualRateCardCycleStartsAtRangeRule(64, 120),
            ManualRateCardCycleLimitRangeRule(1000, 3600),
            ManualRateCardCycleAllowedCodeRule(mutableListOf<String>("strict", "pending")),
            ManualRateCardCycleCodeLengthRule(1000),
            ManualRateCardCycleCodePrefixRule("REF"),
        ),
    )
}
