package com.acme.transport.linehaul.tender.tenderbatch

import com.acme.transport.kit.text.normalize

/** A rule a capped tender window must pass. Check returns its problems, or none. */
interface CappedTenderWindowRule {
    fun check(item: CappedTenderWindow): MutableList<String>
}

data class CappedTenderWindowAllowedPhoneRule(var allowed: MutableList<String>) : CappedTenderWindowRule {
    override fun check(item: CappedTenderWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class CappedTenderWindowNameLengthRule(var maxLength: Int) : CappedTenderWindowRule {
    override fun check(item: CappedTenderWindow): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedTenderWindowQuantityRangeRule(var min: Int, var max: Int) : CappedTenderWindowRule {
    override fun check(item: CappedTenderWindow): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped tender window. */
data class CappedTenderWindowValidator(var rules: MutableList<CappedTenderWindowRule>) {
    fun validate(item: CappedTenderWindow): MutableList<String> {
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

    fun requireValid(item: CappedTenderWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTenderWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTenderWindowValidator(): CappedTenderWindowValidator {
    return CappedTenderWindowValidator(
        mutableListOf<CappedTenderWindowRule>(
            CappedTenderWindowAllowedPhoneRule(mutableListOf<String>()),
            CappedTenderWindowNameLengthRule(255),
            CappedTenderWindowQuantityRangeRule(80, 120),
        ),
    )
}
