package com.acme.transport.customs.clearance.target

import com.acme.transport.kit.text.normalize

/** A rule a primary clearance group must pass. Check returns its problems, or none. */
interface PrimaryClearanceGroupRule {
    fun check(item: PrimaryClearanceGroup): MutableList<String>
}

data class PrimaryClearanceGroupCodeLengthRule(var maxLength: Int) : PrimaryClearanceGroupRule {
    override fun check(item: PrimaryClearanceGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryClearanceGroupAllowedCodeRule(var allowed: MutableList<String>) : PrimaryClearanceGroupRule {
    override fun check(item: PrimaryClearanceGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryClearanceGroupExternalRefLengthRule(var maxLength: Int) : PrimaryClearanceGroupRule {
    override fun check(item: PrimaryClearanceGroup): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryClearanceGroupStartsAtRangeRule(var min: Int, var max: Int) : PrimaryClearanceGroupRule {
    override fun check(item: PrimaryClearanceGroup): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary clearance group. */
data class PrimaryClearanceGroupValidator(var rules: MutableList<PrimaryClearanceGroupRule>) {
    fun validate(item: PrimaryClearanceGroup): MutableList<String> {
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

    fun requireValid(item: PrimaryClearanceGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryClearanceGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryClearanceGroupValidator(): PrimaryClearanceGroupValidator {
    return PrimaryClearanceGroupValidator(
        mutableListOf<PrimaryClearanceGroupRule>(
            PrimaryClearanceGroupCodeLengthRule(64),
            PrimaryClearanceGroupAllowedCodeRule(mutableListOf<String>("rolling")),
            PrimaryClearanceGroupExternalRefLengthRule(255),
            PrimaryClearanceGroupStartsAtRangeRule(64, 80),
        ),
    )
}
