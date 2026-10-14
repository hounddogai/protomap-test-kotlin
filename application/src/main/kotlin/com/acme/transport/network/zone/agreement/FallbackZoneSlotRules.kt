package com.acme.transport.network.zone.agreement

/** A rule a fallback zone slot must pass. Check returns its problems, or none. */
interface FallbackZoneSlotRule {
    fun check(item: FallbackZoneSlot): MutableList<String>
}

data class FallbackZoneSlotWeightRangeRule(var min: Double, var max: Double) : FallbackZoneSlotRule {
    override fun check(item: FallbackZoneSlot): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class FallbackZoneSlotStartsAtRangeRule(var min: Int, var max: Int) : FallbackZoneSlotRule {
    override fun check(item: FallbackZoneSlot): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackZoneSlotMaximumRangeRule(var min: Double, var max: Double) : FallbackZoneSlotRule {
    override fun check(item: FallbackZoneSlot): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback zone slot. */
data class FallbackZoneSlotValidator(var rules: MutableList<FallbackZoneSlotRule>) {
    fun validate(item: FallbackZoneSlot): MutableList<String> {
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

    fun requireValid(item: FallbackZoneSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackZoneSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackZoneSlotValidator(): FallbackZoneSlotValidator {
    return FallbackZoneSlotValidator(
        mutableListOf<FallbackZoneSlotRule>(
            FallbackZoneSlotWeightRangeRule(5.5, 172.3),
            FallbackZoneSlotStartsAtRangeRule(16, 86400),
            FallbackZoneSlotMaximumRangeRule(9.5, 1377.8),
        ),
    )
}
