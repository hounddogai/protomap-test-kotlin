package com.acme.transport.freightpay.dispute.disputeagreement

import com.acme.transport.kit.text.normalize

/** A rule a legacy dispute slot must pass. Check returns its problems, or none. */
interface LegacyDisputeSlotRule {
    fun check(item: LegacyDisputeSlot): MutableList<String>
}

data class LegacyDisputeSlotAllowedCurrencyRule(var allowed: MutableList<String>) : LegacyDisputeSlotRule {
    override fun check(item: LegacyDisputeSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.currency))) {
            return mutableListOf<String>("currency ${item.currency} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDisputeSlotRequiredTitleRule(var message: String) : LegacyDisputeSlotRule {
    override fun check(item: LegacyDisputeSlot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDisputeSlotStopCountRangeRule(var min: Int, var max: Int) : LegacyDisputeSlotRule {
    override fun check(item: LegacyDisputeSlot): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class LegacyDisputeSlotBudgetRangeRule(var min: Double, var max: Double) : LegacyDisputeSlotRule {
    override fun check(item: LegacyDisputeSlot): MutableList<String> {
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

data class LegacyDisputeSlotCurrencyLengthRule(var maxLength: Int) : LegacyDisputeSlotRule {
    override fun check(item: LegacyDisputeSlot): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDisputeSlotRequiredCodeRule(var message: String) : LegacyDisputeSlotRule {
    override fun check(item: LegacyDisputeSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy dispute slot. */
data class LegacyDisputeSlotValidator(var rules: MutableList<LegacyDisputeSlotRule>) {
    fun validate(item: LegacyDisputeSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyDisputeSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDisputeSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDisputeSlotValidator(): LegacyDisputeSlotValidator {
    return LegacyDisputeSlotValidator(
        mutableListOf<LegacyDisputeSlotRule>(
            LegacyDisputeSlotAllowedCurrencyRule(mutableListOf<String>()),
            LegacyDisputeSlotRequiredTitleRule("legacy dispute slot needs this field"),
            LegacyDisputeSlotStopCountRangeRule(64, 255),
            LegacyDisputeSlotBudgetRangeRule(9.3, 2409.9),
            LegacyDisputeSlotCurrencyLengthRule(3600),
            LegacyDisputeSlotRequiredCodeRule("legacy dispute slot needs a value"),
        ),
    )
}
