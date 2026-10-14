package com.acme.transport.lastmile.locker.group

import com.acme.transport.kit.text.normalize

/** A rule a legacy locker slot must pass. Check returns its problems, or none. */
interface LegacyLockerSlotRule {
    fun check(item: LegacyLockerSlot): MutableList<String>
}

data class LegacyLockerSlotRequiredPhoneRule(var message: String) : LegacyLockerSlotRule {
    override fun check(item: LegacyLockerSlot): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyLockerSlotVisibleGuardRule(var limit: Double) : LegacyLockerSlotRule {
    override fun check(item: LegacyLockerSlot): MutableList<String> {
        if (item.visible && item.cost > limit) {
            return mutableListOf<String>("cost ${item.cost} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLockerSlotRequiredLabelRule(var message: String) : LegacyLockerSlotRule {
    override fun check(item: LegacyLockerSlot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyLockerSlotAllowedPhoneRule(var allowed: MutableList<String>) : LegacyLockerSlotRule {
    override fun check(item: LegacyLockerSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLockerSlotPhoneLengthRule(var maxLength: Int) : LegacyLockerSlotRule {
    override fun check(item: LegacyLockerSlot): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy locker slot. */
data class LegacyLockerSlotValidator(var rules: MutableList<LegacyLockerSlotRule>) {
    fun validate(item: LegacyLockerSlot): MutableList<String> {
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

    fun requireValid(item: LegacyLockerSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyLockerSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyLockerSlotValidator(): LegacyLockerSlotValidator {
    return LegacyLockerSlotValidator(
        mutableListOf<LegacyLockerSlotRule>(
            LegacyLockerSlotRequiredPhoneRule("legacy locker slot needs an entry"),
            LegacyLockerSlotVisibleGuardRule(4.7),
            LegacyLockerSlotRequiredLabelRule("legacy locker slot needs this field"),
            LegacyLockerSlotAllowedPhoneRule(mutableListOf<String>("tiered", "global")),
            LegacyLockerSlotPhoneLengthRule(32),
        ),
    )
}
