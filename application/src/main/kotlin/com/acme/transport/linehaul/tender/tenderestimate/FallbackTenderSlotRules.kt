package com.acme.transport.linehaul.tender.tenderestimate

import com.acme.transport.kit.text.normalize

/** A rule a fallback tender slot must pass. Check returns its problems, or none. */
interface FallbackTenderSlotRule {
    fun check(item: FallbackTenderSlot): MutableList<String>
}

data class FallbackTenderSlotRequiredCodeRule(var message: String) : FallbackTenderSlotRule {
    override fun check(item: FallbackTenderSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTenderSlotAllowedShiftCodeRule(var allowed: MutableList<String>) : FallbackTenderSlotRule {
    override fun check(item: FallbackTenderSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTenderSlotRequiredPhoneRule(var message: String) : FallbackTenderSlotRule {
    override fun check(item: FallbackTenderSlot): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTenderSlotTitleLengthRule(var maxLength: Int) : FallbackTenderSlotRule {
    override fun check(item: FallbackTenderSlot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTenderSlotLocaleLengthRule(var maxLength: Int) : FallbackTenderSlotRule {
    override fun check(item: FallbackTenderSlot): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback tender slot. */
data class FallbackTenderSlotValidator(var rules: MutableList<FallbackTenderSlotRule>) {
    fun validate(item: FallbackTenderSlot): MutableList<String> {
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

    fun requireValid(item: FallbackTenderSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackTenderSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackTenderSlotValidator(): FallbackTenderSlotValidator {
    return FallbackTenderSlotValidator(
        mutableListOf<FallbackTenderSlotRule>(
            FallbackTenderSlotRequiredCodeRule("fallback tender slot needs this field"),
            FallbackTenderSlotAllowedShiftCodeRule(mutableListOf<String>("lenient", "batched")),
            FallbackTenderSlotRequiredPhoneRule("fallback tender slot needs this field"),
            FallbackTenderSlotTitleLengthRule(16),
            FallbackTenderSlotLocaleLengthRule(32),
        ),
    )
}
