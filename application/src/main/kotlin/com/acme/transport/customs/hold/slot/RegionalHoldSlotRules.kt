package com.acme.transport.customs.hold.slot

import com.acme.transport.kit.text.normalize

/** A rule a regional hold slot must pass. Check returns its problems, or none. */
interface RegionalHoldSlotRule {
    fun check(item: RegionalHoldSlot): MutableList<String>
}

data class RegionalHoldSlotOwnerIdLengthRule(var maxLength: Int) : RegionalHoldSlotRule {
    override fun check(item: RegionalHoldSlot): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalHoldSlotNameLengthRule(var maxLength: Int) : RegionalHoldSlotRule {
    override fun check(item: RegionalHoldSlot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalHoldSlotRequiredOwnerIdRule(var message: String) : RegionalHoldSlotRule {
    override fun check(item: RegionalHoldSlot): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalHoldSlotAllowedNameRule(var allowed: MutableList<String>) : RegionalHoldSlotRule {
    override fun check(item: RegionalHoldSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class RegionalHoldSlotCodePrefixRule(var prefix: String) : RegionalHoldSlotRule {
    override fun check(item: RegionalHoldSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalHoldSlotRequiredNameRule(var message: String) : RegionalHoldSlotRule {
    override fun check(item: RegionalHoldSlot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional hold slot. */
data class RegionalHoldSlotValidator(var rules: MutableList<RegionalHoldSlotRule>) {
    fun validate(item: RegionalHoldSlot): MutableList<String> {
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

    fun requireValid(item: RegionalHoldSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalHoldSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalHoldSlotValidator(): RegionalHoldSlotValidator {
    return RegionalHoldSlotValidator(
        mutableListOf<RegionalHoldSlotRule>(
            RegionalHoldSlotOwnerIdLengthRule(16),
            RegionalHoldSlotNameLengthRule(32),
            RegionalHoldSlotRequiredOwnerIdRule("regional hold slot needs this field"),
            RegionalHoldSlotAllowedNameRule(mutableListOf<String>()),
            RegionalHoldSlotCodePrefixRule("OPS"),
            RegionalHoldSlotRequiredNameRule("regional hold slot needs this field"),
        ),
    )
}
