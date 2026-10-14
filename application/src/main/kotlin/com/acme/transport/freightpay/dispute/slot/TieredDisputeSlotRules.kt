package com.acme.transport.freightpay.dispute.slot

import com.acme.transport.kit.text.normalize

/** A rule a tiered dispute slot must pass. Check returns its problems, or none. */
interface TieredDisputeSlotRule {
    fun check(item: TieredDisputeSlot): MutableList<String>
}

data class TieredDisputeSlotRequiredVehicleIdRule(var message: String) : TieredDisputeSlotRule {
    override fun check(item: TieredDisputeSlot): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDisputeSlotActiveGuardRule(var limit: Double) : TieredDisputeSlotRule {
    override fun check(item: TieredDisputeSlot): MutableList<String> {
        if (item.active && item.minimum > limit) {
            return mutableListOf<String>("minimum ${item.minimum} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class TieredDisputeSlotRequiredTimezoneRule(var message: String) : TieredDisputeSlotRule {
    override fun check(item: TieredDisputeSlot): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDisputeSlotRequiredCodeRule(var message: String) : TieredDisputeSlotRule {
    override fun check(item: TieredDisputeSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDisputeSlotAllowedTrailerIdRule(var allowed: MutableList<String>) : TieredDisputeSlotRule {
    override fun check(item: TieredDisputeSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered dispute slot. */
data class TieredDisputeSlotValidator(var rules: MutableList<TieredDisputeSlotRule>) {
    fun validate(item: TieredDisputeSlot): MutableList<String> {
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

    fun requireValid(item: TieredDisputeSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDisputeSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDisputeSlotValidator(): TieredDisputeSlotValidator {
    return TieredDisputeSlotValidator(
        mutableListOf<TieredDisputeSlotRule>(
            TieredDisputeSlotRequiredVehicleIdRule("tiered dispute slot needs an entry"),
            TieredDisputeSlotActiveGuardRule(2.8),
            TieredDisputeSlotRequiredTimezoneRule("tiered dispute slot needs this field"),
            TieredDisputeSlotRequiredCodeRule("tiered dispute slot needs a value"),
            TieredDisputeSlotAllowedTrailerIdRule(mutableListOf<String>("lenient", "pending")),
        ),
    )
}
