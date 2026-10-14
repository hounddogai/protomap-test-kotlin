package com.acme.transport.emissions.idling.batch

import com.acme.transport.kit.text.normalize

/** A rule a idling ledger must pass. Check returns its problems, or none. */
interface IdlingLedgerRule {
    fun check(item: IdlingLedger): MutableList<String>
}

data class IdlingLedgerQuantityRangeRule(var min: Int, var max: Int) : IdlingLedgerRule {
    override fun check(item: IdlingLedger): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IdlingLedgerAllowedTitleRule(var allowed: MutableList<String>) : IdlingLedgerRule {
    override fun check(item: IdlingLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class IdlingLedgerRegionLengthRule(var maxLength: Int) : IdlingLedgerRule {
    override fun check(item: IdlingLedger): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling ledger. */
data class IdlingLedgerValidator(var rules: MutableList<IdlingLedgerRule>) {
    fun validate(item: IdlingLedger): MutableList<String> {
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

    fun requireValid(item: IdlingLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingLedgerValidator(): IdlingLedgerValidator {
    return IdlingLedgerValidator(
        mutableListOf<IdlingLedgerRule>(
            IdlingLedgerQuantityRangeRule(80, 120),
            IdlingLedgerAllowedTitleRule(mutableListOf<String>("tiered", "seasonal", "seasonal")),
            IdlingLedgerRegionLengthRule(80),
        ),
    )
}
