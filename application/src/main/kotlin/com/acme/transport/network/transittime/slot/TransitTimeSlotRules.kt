package com.acme.transport.network.transittime.slot

import com.acme.transport.kit.text.normalize

/** A rule a transit time slot must pass. Check returns its problems, or none. */
interface TransitTimeSlotRule {
    fun check(item: TransitTimeSlot): MutableList<String>
}

data class TransitTimeSlotRequiredNotesRule(var message: String) : TransitTimeSlotRule {
    override fun check(item: TransitTimeSlot): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeSlotTitleLengthRule(var maxLength: Int) : TransitTimeSlotRule {
    override fun check(item: TransitTimeSlot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeSlotCodePrefixRule(var prefix: String) : TransitTimeSlotRule {
    override fun check(item: TransitTimeSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeSlotRequiredChecksumRule(var message: String) : TransitTimeSlotRule {
    override fun check(item: TransitTimeSlot): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeSlotAllowedNotesRule(var allowed: MutableList<String>) : TransitTimeSlotRule {
    override fun check(item: TransitTimeSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeSlotChecksumLengthRule(var maxLength: Int) : TransitTimeSlotRule {
    override fun check(item: TransitTimeSlot): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time slot. */
data class TransitTimeSlotValidator(var rules: MutableList<TransitTimeSlotRule>) {
    fun validate(item: TransitTimeSlot): MutableList<String> {
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

    fun requireValid(item: TransitTimeSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeSlotValidator(): TransitTimeSlotValidator {
    return TransitTimeSlotValidator(
        mutableListOf<TransitTimeSlotRule>(
            TransitTimeSlotRequiredNotesRule("transit time slot needs an entry"),
            TransitTimeSlotTitleLengthRule(16),
            TransitTimeSlotCodePrefixRule("ACM"),
            TransitTimeSlotRequiredChecksumRule("transit time slot needs a value"),
            TransitTimeSlotAllowedNotesRule(mutableListOf<String>()),
            TransitTimeSlotChecksumLengthRule(86400),
        ),
    )
}
