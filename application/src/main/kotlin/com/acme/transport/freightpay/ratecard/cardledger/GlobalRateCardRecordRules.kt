package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.kit.text.normalize

/** A rule a global rate card record must pass. Check returns its problems, or none. */
interface GlobalRateCardRecordRule {
    fun check(item: GlobalRateCardRecord): MutableList<String>
}

data class GlobalRateCardRecordRequiredExternalRefRule(var message: String) : GlobalRateCardRecordRule {
    override fun check(item: GlobalRateCardRecord): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRateCardRecordAllowedCodeRule(var allowed: MutableList<String>) : GlobalRateCardRecordRule {
    override fun check(item: GlobalRateCardRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRateCardRecordCostRangeRule(var min: Double, var max: Double) : GlobalRateCardRecordRule {
    override fun check(item: GlobalRateCardRecord): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRateCardRecordCodeLengthRule(var maxLength: Int) : GlobalRateCardRecordRule {
    override fun check(item: GlobalRateCardRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRateCardRecordLabelLengthRule(var maxLength: Int) : GlobalRateCardRecordRule {
    override fun check(item: GlobalRateCardRecord): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRateCardRecordExternalRefLengthRule(var maxLength: Int) : GlobalRateCardRecordRule {
    override fun check(item: GlobalRateCardRecord): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global rate card record. */
data class GlobalRateCardRecordValidator(var rules: MutableList<GlobalRateCardRecordRule>) {
    fun validate(item: GlobalRateCardRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalRateCardRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRateCardRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRateCardRecordValidator(): GlobalRateCardRecordValidator {
    return GlobalRateCardRecordValidator(
        mutableListOf<GlobalRateCardRecordRule>(
            GlobalRateCardRecordRequiredExternalRefRule("global rate card record needs a value"),
            GlobalRateCardRecordAllowedCodeRule(mutableListOf<String>()),
            GlobalRateCardRecordCostRangeRule(4.2, 84.5),
            GlobalRateCardRecordCodeLengthRule(86400),
            GlobalRateCardRecordLabelLengthRule(120),
            GlobalRateCardRecordExternalRefLengthRule(255),
        ),
    )
}
