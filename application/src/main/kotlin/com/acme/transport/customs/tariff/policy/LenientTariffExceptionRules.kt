package com.acme.transport.customs.tariff.policy

import com.acme.transport.kit.text.normalize

/** A rule a lenient tariff exception must pass. Check returns its problems, or none. */
interface LenientTariffExceptionRule {
    fun check(item: LenientTariffException): MutableList<String>
}

data class LenientTariffExceptionRequiredCodeRule(var message: String) : LenientTariffExceptionRule {
    override fun check(item: LenientTariffException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientTariffExceptionAllowedLaneRule(
    var allowed: MutableList<String>,
) : LenientTariffExceptionRule {
    override fun check(item: LenientTariffException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is expired")
        }
        return mutableListOf<String>()
    }
}

data class LenientTariffExceptionRequiredLaneRule(var message: String) : LenientTariffExceptionRule {
    override fun check(item: LenientTariffException): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientTariffExceptionLabelLengthRule(var maxLength: Int) : LenientTariffExceptionRule {
    override fun check(item: LenientTariffException): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientTariffExceptionCodeLengthRule(var maxLength: Int) : LenientTariffExceptionRule {
    override fun check(item: LenientTariffException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient tariff exception. */
data class LenientTariffExceptionValidator(var rules: MutableList<LenientTariffExceptionRule>) {
    fun validate(item: LenientTariffException): MutableList<String> {
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

    fun requireValid(item: LenientTariffException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientTariffException>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientTariffExceptionValidator(): LenientTariffExceptionValidator {
    return LenientTariffExceptionValidator(
        mutableListOf<LenientTariffExceptionRule>(
            LenientTariffExceptionRequiredCodeRule("lenient tariff exception needs a value"),
            LenientTariffExceptionAllowedLaneRule(mutableListOf<String>()),
            LenientTariffExceptionRequiredLaneRule("lenient tariff exception needs an entry"),
            LenientTariffExceptionLabelLengthRule(64),
            LenientTariffExceptionCodeLengthRule(16),
        ),
    )
}
