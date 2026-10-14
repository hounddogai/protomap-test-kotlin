package com.acme.transport.crewing.swap.plan

import com.acme.transport.kit.text.normalize

/** A rule a swap slot must pass. Check returns its problems, or none. */
interface SwapSlotRule {
    fun check(item: SwapSlot): MutableList<String>
}

data class SwapSlotAllowedLabelRule(var allowed: MutableList<String>) : SwapSlotRule {
    override fun check(item: SwapSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class SwapSlotRequiredLabelRule(var message: String) : SwapSlotRule {
    override fun check(item: SwapSlot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapSlotCodePrefixRule(var prefix: String) : SwapSlotRule {
    override fun check(item: SwapSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SwapSlotLabelLengthRule(var maxLength: Int) : SwapSlotRule {
    override fun check(item: SwapSlot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapSlotOwnerIdLengthRule(var maxLength: Int) : SwapSlotRule {
    override fun check(item: SwapSlot): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapSlotRequiredOwnerIdRule(var message: String) : SwapSlotRule {
    override fun check(item: SwapSlot): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap slot. */
data class SwapSlotValidator(var rules: MutableList<SwapSlotRule>) {
    fun validate(item: SwapSlot): MutableList<String> {
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

    fun requireValid(item: SwapSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapSlotValidator(): SwapSlotValidator {
    return SwapSlotValidator(
        mutableListOf<SwapSlotRule>(
            SwapSlotAllowedLabelRule(mutableListOf<String>("primary", "strict", "pending")),
            SwapSlotRequiredLabelRule("swap slot needs this field"),
            SwapSlotCodePrefixRule("X"),
            SwapSlotLabelLengthRule(80),
            SwapSlotOwnerIdLengthRule(120),
            SwapSlotRequiredOwnerIdRule("swap slot needs this field"),
        ),
    )
}
