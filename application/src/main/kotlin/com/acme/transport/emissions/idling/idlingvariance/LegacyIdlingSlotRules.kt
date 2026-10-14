package com.acme.transport.emissions.idling.idlingvariance

/** A rule a legacy idling slot must pass. Check returns its problems, or none. */
interface LegacyIdlingSlotRule {
    fun check(item: LegacyIdlingSlot): MutableList<String>
}

data class LegacyIdlingSlotRequiredCodeRule(var message: String) : LegacyIdlingSlotRule {
    override fun check(item: LegacyIdlingSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyIdlingSlotRequiredTimezoneRule(var message: String) : LegacyIdlingSlotRule {
    override fun check(item: LegacyIdlingSlot): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyIdlingSlotThresholdRangeRule(var min: Double, var max: Double) : LegacyIdlingSlotRule {
    override fun check(item: LegacyIdlingSlot): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class LegacyIdlingSlotCodePrefixRule(var prefix: String) : LegacyIdlingSlotRule {
    override fun check(item: LegacyIdlingSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy idling slot. */
data class LegacyIdlingSlotValidator(var rules: MutableList<LegacyIdlingSlotRule>) {
    fun validate(item: LegacyIdlingSlot): MutableList<String> {
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

    fun requireValid(item: LegacyIdlingSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyIdlingSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyIdlingSlotValidator(): LegacyIdlingSlotValidator {
    return LegacyIdlingSlotValidator(
        mutableListOf<LegacyIdlingSlotRule>(
            LegacyIdlingSlotRequiredCodeRule("legacy idling slot needs a value"),
            LegacyIdlingSlotRequiredTimezoneRule("legacy idling slot needs this field"),
            LegacyIdlingSlotThresholdRangeRule(5.7, 2904.3),
            LegacyIdlingSlotCodePrefixRule("Q"),
        ),
    )
}
