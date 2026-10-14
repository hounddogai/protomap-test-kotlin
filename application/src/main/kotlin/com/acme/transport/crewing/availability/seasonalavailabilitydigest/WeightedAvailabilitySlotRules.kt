package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import com.acme.transport.kit.text.normalize

/** A rule a weighted availability slot must pass. Check returns its problems, or none. */
interface WeightedAvailabilitySlotRule {
    fun check(item: WeightedAvailabilitySlot): MutableList<String>
}

data class WeightedAvailabilitySlotCarrierLengthRule(var maxLength: Int) : WeightedAvailabilitySlotRule {
    override fun check(item: WeightedAvailabilitySlot): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAvailabilitySlotAllowedCodeRule(
    var allowed: MutableList<String>,
) : WeightedAvailabilitySlotRule {
    override fun check(item: WeightedAvailabilitySlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAvailabilitySlotRequiredTimezoneRule(var message: String) : WeightedAvailabilitySlotRule {
    override fun check(item: WeightedAvailabilitySlot): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted availability slot. */
data class WeightedAvailabilitySlotValidator(var rules: MutableList<WeightedAvailabilitySlotRule>) {
    fun validate(item: WeightedAvailabilitySlot): MutableList<String> {
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

    fun requireValid(item: WeightedAvailabilitySlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedAvailabilitySlot>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedAvailabilitySlotValidator(): WeightedAvailabilitySlotValidator {
    return WeightedAvailabilitySlotValidator(
        mutableListOf<WeightedAvailabilitySlotRule>(
            WeightedAvailabilitySlotCarrierLengthRule(255),
            WeightedAvailabilitySlotAllowedCodeRule(mutableListOf<String>("seasonal", "global")),
            WeightedAvailabilitySlotRequiredTimezoneRule("weighted availability slot needs this field"),
        ),
    )
}
