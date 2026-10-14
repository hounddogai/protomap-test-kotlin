package com.acme.transport.safety.cameraevent.profile

import com.acme.transport.kit.text.normalize

/** A rule a tiered camera event slot must pass. Check returns its problems, or none. */
interface TieredCameraEventSlotRule {
    fun check(item: TieredCameraEventSlot): MutableList<String>
}

data class TieredCameraEventSlotAllowedNameRule(var allowed: MutableList<String>) : TieredCameraEventSlotRule {
    override fun check(item: TieredCameraEventSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class TieredCameraEventSlotRequiredCodeRule(var message: String) : TieredCameraEventSlotRule {
    override fun check(item: TieredCameraEventSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCameraEventSlotRequiredShiftCodeRule(var message: String) : TieredCameraEventSlotRule {
    override fun check(item: TieredCameraEventSlot): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCameraEventSlotCodePrefixRule(var prefix: String) : TieredCameraEventSlotRule {
    override fun check(item: TieredCameraEventSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredCameraEventSlotCostRangeRule(var min: Double, var max: Double) : TieredCameraEventSlotRule {
    override fun check(item: TieredCameraEventSlot): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredCameraEventSlotWindowDaysRangeRule(var min: Int, var max: Int) : TieredCameraEventSlotRule {
    override fun check(item: TieredCameraEventSlot): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tiered camera event slot. */
data class TieredCameraEventSlotValidator(var rules: MutableList<TieredCameraEventSlotRule>) {
    fun validate(item: TieredCameraEventSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredCameraEventSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCameraEventSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCameraEventSlotValidator(): TieredCameraEventSlotValidator {
    return TieredCameraEventSlotValidator(
        mutableListOf<TieredCameraEventSlotRule>(
            TieredCameraEventSlotAllowedNameRule(mutableListOf<String>("lenient", "strict")),
            TieredCameraEventSlotRequiredCodeRule("tiered camera event slot needs an entry"),
            TieredCameraEventSlotRequiredShiftCodeRule("tiered camera event slot needs a value"),
            TieredCameraEventSlotCodePrefixRule("Q"),
            TieredCameraEventSlotCostRangeRule(8.8, 3537.7),
            TieredCameraEventSlotWindowDaysRangeRule(64, 80),
        ),
    )
}
