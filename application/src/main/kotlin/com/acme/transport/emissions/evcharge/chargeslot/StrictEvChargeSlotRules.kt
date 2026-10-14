package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.kit.text.normalize

/** A rule a strict ev charge slot must pass. Check returns its problems, or none. */
interface StrictEvChargeSlotRule {
    fun check(item: StrictEvChargeSlot): MutableList<String>
}

data class StrictEvChargeSlotAllowedCodeRule(var allowed: MutableList<String>) : StrictEvChargeSlotRule {
    override fun check(item: StrictEvChargeSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class StrictEvChargeSlotCodeLengthRule(var maxLength: Int) : StrictEvChargeSlotRule {
    override fun check(item: StrictEvChargeSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictEvChargeSlotGraceDaysRangeRule(var min: Int, var max: Int) : StrictEvChargeSlotRule {
    override fun check(item: StrictEvChargeSlot): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictEvChargeSlotNameLengthRule(var maxLength: Int) : StrictEvChargeSlotRule {
    override fun check(item: StrictEvChargeSlot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictEvChargeSlotMaximumRangeRule(var min: Double, var max: Double) : StrictEvChargeSlotRule {
    override fun check(item: StrictEvChargeSlot): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class StrictEvChargeSlotApprovedGuardRule(var limit: Int) : StrictEvChargeSlotRule {
    override fun check(item: StrictEvChargeSlot): MutableList<String> {
        if (item.approved && item.startsAt > limit) {
            return mutableListOf<String>("starts at ${item.startsAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict ev charge slot. */
data class StrictEvChargeSlotValidator(var rules: MutableList<StrictEvChargeSlotRule>) {
    fun validate(item: StrictEvChargeSlot): MutableList<String> {
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

    fun requireValid(item: StrictEvChargeSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictEvChargeSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictEvChargeSlotValidator(): StrictEvChargeSlotValidator {
    return StrictEvChargeSlotValidator(
        mutableListOf<StrictEvChargeSlotRule>(
            StrictEvChargeSlotAllowedCodeRule(mutableListOf<String>()),
            StrictEvChargeSlotCodeLengthRule(64),
            StrictEvChargeSlotGraceDaysRangeRule(64, 80),
            StrictEvChargeSlotNameLengthRule(80),
            StrictEvChargeSlotMaximumRangeRule(0.7, 2000.5),
            StrictEvChargeSlotApprovedGuardRule(3600),
        ),
    )
}
