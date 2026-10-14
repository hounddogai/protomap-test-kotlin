package com.acme.transport.terminal.crossdock.dockslot

import com.acme.transport.kit.text.normalize

/** A rule a legacy cross dock exception must pass. Check returns its problems, or none. */
interface LegacyCrossDockExceptionRule {
    fun check(item: LegacyCrossDockException): MutableList<String>
}

data class LegacyCrossDockExceptionSequenceRangeRule(
    var min: Int,
    var max: Int,
) : LegacyCrossDockExceptionRule {
    override fun check(item: LegacyCrossDockException): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCrossDockExceptionExternalRefLengthRule(var maxLength: Int) : LegacyCrossDockExceptionRule {
    override fun check(item: LegacyCrossDockException): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCrossDockExceptionPalletsRangeRule(var min: Int, var max: Int) : LegacyCrossDockExceptionRule {
    override fun check(item: LegacyCrossDockException): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCrossDockExceptionAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : LegacyCrossDockExceptionRule {
    override fun check(item: LegacyCrossDockException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCrossDockExceptionRequiredExternalRefRule(var message: String) : LegacyCrossDockExceptionRule {
    override fun check(item: LegacyCrossDockException): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyCrossDockExceptionAmountRangeRule(
    var min: Double,
    var max: Double,
) : LegacyCrossDockExceptionRule {
    override fun check(item: LegacyCrossDockException): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy cross dock exception. */
data class LegacyCrossDockExceptionValidator(var rules: MutableList<LegacyCrossDockExceptionRule>) {
    fun validate(item: LegacyCrossDockException): MutableList<String> {
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

    fun requireValid(item: LegacyCrossDockException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyCrossDockException>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyCrossDockExceptionValidator(): LegacyCrossDockExceptionValidator {
    return LegacyCrossDockExceptionValidator(
        mutableListOf<LegacyCrossDockExceptionRule>(
            LegacyCrossDockExceptionSequenceRangeRule(16, 255),
            LegacyCrossDockExceptionExternalRefLengthRule(120),
            LegacyCrossDockExceptionPalletsRangeRule(32, 120),
            LegacyCrossDockExceptionAllowedOwnerIdRule(mutableListOf<String>("seasonal")),
            LegacyCrossDockExceptionRequiredExternalRefRule("legacy cross dock exception needs a value"),
            LegacyCrossDockExceptionAmountRangeRule(8.4, 2902.8),
        ),
    )
}
