package com.acme.transport.network.transittime.snapshot

/** A rule a manual transit time slot must pass. Check returns its problems, or none. */
interface ManualTransitTimeSlotRule {
    fun check(item: ManualTransitTimeSlot): MutableList<String>
}

data class ManualTransitTimeSlotLoadKgRangeRule(var min: Double, var max: Double) : ManualTransitTimeSlotRule {
    override fun check(item: ManualTransitTimeSlot): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class ManualTransitTimeSlotCodePrefixRule(var prefix: String) : ManualTransitTimeSlotRule {
    override fun check(item: ManualTransitTimeSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualTransitTimeSlotCodeLengthRule(var maxLength: Int) : ManualTransitTimeSlotRule {
    override fun check(item: ManualTransitTimeSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTransitTimeSlotRequiredCodeRule(var message: String) : ManualTransitTimeSlotRule {
    override fun check(item: ManualTransitTimeSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTransitTimeSlotLimitRangeRule(var min: Int, var max: Int) : ManualTransitTimeSlotRule {
    override fun check(item: ManualTransitTimeSlot): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a manual transit time slot. */
data class ManualTransitTimeSlotValidator(var rules: MutableList<ManualTransitTimeSlotRule>) {
    fun validate(item: ManualTransitTimeSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualTransitTimeSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTransitTimeSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTransitTimeSlotValidator(): ManualTransitTimeSlotValidator {
    return ManualTransitTimeSlotValidator(
        mutableListOf<ManualTransitTimeSlotRule>(
            ManualTransitTimeSlotLoadKgRangeRule(0.5, 2355.0),
            ManualTransitTimeSlotCodePrefixRule("REF"),
            ManualTransitTimeSlotCodeLengthRule(255),
            ManualTransitTimeSlotRequiredCodeRule("manual transit time slot needs an entry"),
            ManualTransitTimeSlotLimitRangeRule(255, 86400),
        ),
    )
}
