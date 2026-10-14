package com.acme.transport.emissions.evcharge.batchedchargeslot

/** A rule a fallback ev charge slot must pass. Check returns its problems, or none. */
interface FallbackEvChargeSlotRule {
    fun check(item: FallbackEvChargeSlot): MutableList<String>
}

data class FallbackEvChargeSlotCodePrefixRule(var prefix: String) : FallbackEvChargeSlotRule {
    override fun check(item: FallbackEvChargeSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEvChargeSlotCodeLengthRule(var maxLength: Int) : FallbackEvChargeSlotRule {
    override fun check(item: FallbackEvChargeSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEvChargeSlotDurationMinutesRangeRule(var min: Int, var max: Int) : FallbackEvChargeSlotRule {
    override fun check(item: FallbackEvChargeSlot): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class FallbackEvChargeSlotLabelLengthRule(var maxLength: Int) : FallbackEvChargeSlotRule {
    override fun check(item: FallbackEvChargeSlot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEvChargeSlotDepotLengthRule(var maxLength: Int) : FallbackEvChargeSlotRule {
    override fun check(item: FallbackEvChargeSlot): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEvChargeSlotChannelLengthRule(var maxLength: Int) : FallbackEvChargeSlotRule {
    override fun check(item: FallbackEvChargeSlot): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback ev charge slot. */
data class FallbackEvChargeSlotValidator(var rules: MutableList<FallbackEvChargeSlotRule>) {
    fun validate(item: FallbackEvChargeSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackEvChargeSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackEvChargeSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackEvChargeSlotValidator(): FallbackEvChargeSlotValidator {
    return FallbackEvChargeSlotValidator(
        mutableListOf<FallbackEvChargeSlotRule>(
            FallbackEvChargeSlotCodePrefixRule("REF"),
            FallbackEvChargeSlotCodeLengthRule(80),
            FallbackEvChargeSlotDurationMinutesRangeRule(64, 64),
            FallbackEvChargeSlotLabelLengthRule(64),
            FallbackEvChargeSlotDepotLengthRule(16),
            FallbackEvChargeSlotChannelLengthRule(80),
        ),
    )
}
