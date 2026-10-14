package com.acme.transport.freightpay.ratecard.variance

import com.acme.transport.kit.text.normalize

/** A rule a manual rate card threshold must pass. Check returns its problems, or none. */
interface ManualRateCardThresholdRule {
    fun check(item: ManualRateCardThreshold): MutableList<String>
}

data class ManualRateCardThresholdCodeLengthRule(var maxLength: Int) : ManualRateCardThresholdRule {
    override fun check(item: ManualRateCardThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualRateCardThresholdPalletsRangeRule(var min: Int, var max: Int) : ManualRateCardThresholdRule {
    override fun check(item: ManualRateCardThreshold): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualRateCardThresholdBudgetRangeRule(
    var min: Double,
    var max: Double,
) : ManualRateCardThresholdRule {
    override fun check(item: ManualRateCardThreshold): MutableList<String> {
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

data class ManualRateCardThresholdChecksumLengthRule(var maxLength: Int) : ManualRateCardThresholdRule {
    override fun check(item: ManualRateCardThreshold): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualRateCardThresholdAllowedNameRule(
    var allowed: MutableList<String>,
) : ManualRateCardThresholdRule {
    override fun check(item: ManualRateCardThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class ManualRateCardThresholdRequiredCodeRule(var message: String) : ManualRateCardThresholdRule {
    override fun check(item: ManualRateCardThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual rate card threshold. */
data class ManualRateCardThresholdValidator(var rules: MutableList<ManualRateCardThresholdRule>) {
    fun validate(item: ManualRateCardThreshold): MutableList<String> {
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

    fun requireValid(item: ManualRateCardThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualRateCardThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultManualRateCardThresholdValidator(): ManualRateCardThresholdValidator {
    return ManualRateCardThresholdValidator(
        mutableListOf<ManualRateCardThresholdRule>(
            ManualRateCardThresholdCodeLengthRule(1000),
            ManualRateCardThresholdPalletsRangeRule(32, 64),
            ManualRateCardThresholdBudgetRangeRule(6.8, 4627.3),
            ManualRateCardThresholdChecksumLengthRule(120),
            ManualRateCardThresholdAllowedNameRule(mutableListOf<String>("capped")),
            ManualRateCardThresholdRequiredCodeRule("manual rate card threshold needs a value"),
        ),
    )
}
