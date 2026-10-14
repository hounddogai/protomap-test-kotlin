package com.acme.transport.linehaul.capacity.slot

import com.acme.transport.kit.text.normalize

/** A rule a fallback capacity slot must pass. Check returns its problems, or none. */
interface FallbackCapacitySlotRule {
    fun check(item: FallbackCapacitySlot): MutableList<String>
}

data class FallbackCapacitySlotPalletsRangeRule(var min: Int, var max: Int) : FallbackCapacitySlotRule {
    override fun check(item: FallbackCapacitySlot): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCapacitySlotAllowedLabelRule(var allowed: MutableList<String>) : FallbackCapacitySlotRule {
    override fun check(item: FallbackCapacitySlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is missing")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCapacitySlotReviewerLengthRule(var maxLength: Int) : FallbackCapacitySlotRule {
    override fun check(item: FallbackCapacitySlot): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCapacitySlotCodePrefixRule(var prefix: String) : FallbackCapacitySlotRule {
    override fun check(item: FallbackCapacitySlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback capacity slot. */
data class FallbackCapacitySlotValidator(var rules: MutableList<FallbackCapacitySlotRule>) {
    fun validate(item: FallbackCapacitySlot): MutableList<String> {
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

    fun requireValid(item: FallbackCapacitySlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackCapacitySlot>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackCapacitySlotValidator(): FallbackCapacitySlotValidator {
    return FallbackCapacitySlotValidator(
        mutableListOf<FallbackCapacitySlotRule>(
            FallbackCapacitySlotPalletsRangeRule(80, 3600),
            FallbackCapacitySlotAllowedLabelRule(mutableListOf<String>("strict", "strict", "batched")),
            FallbackCapacitySlotReviewerLengthRule(86400),
            FallbackCapacitySlotCodePrefixRule("OPS"),
        ),
    )
}
