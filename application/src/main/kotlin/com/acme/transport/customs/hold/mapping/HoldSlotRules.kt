package com.acme.transport.customs.hold.mapping

import com.acme.transport.kit.text.normalize

/** A rule a hold slot must pass. Check returns its problems, or none. */
interface HoldSlotRule {
    fun check(item: HoldSlot): MutableList<String>
}

data class HoldSlotAllowedCodeRule(var allowed: MutableList<String>) : HoldSlotRule {
    override fun check(item: HoldSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class HoldSlotCodeLengthRule(var maxLength: Int) : HoldSlotRule {
    override fun check(item: HoldSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldSlotRevisionRangeRule(var min: Int, var max: Int) : HoldSlotRule {
    override fun check(item: HoldSlot): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HoldSlotRequiredTimezoneRule(var message: String) : HoldSlotRule {
    override fun check(item: HoldSlot): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldSlotTimezoneLengthRule(var maxLength: Int) : HoldSlotRule {
    override fun check(item: HoldSlot): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold slot. */
data class HoldSlotValidator(var rules: MutableList<HoldSlotRule>) {
    fun validate(item: HoldSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HoldSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldSlotValidator(): HoldSlotValidator {
    return HoldSlotValidator(
        mutableListOf<HoldSlotRule>(
            HoldSlotAllowedCodeRule(mutableListOf<String>("batched", "lenient")),
            HoldSlotCodeLengthRule(64),
            HoldSlotRevisionRangeRule(64, 120),
            HoldSlotRequiredTimezoneRule("hold slot needs an entry"),
            HoldSlotTimezoneLengthRule(32),
        ),
    )
}
