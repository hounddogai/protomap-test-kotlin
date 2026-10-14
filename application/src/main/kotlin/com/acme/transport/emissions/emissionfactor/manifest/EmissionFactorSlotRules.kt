package com.acme.transport.emissions.emissionfactor.manifest

/** A rule a emission factor slot must pass. Check returns its problems, or none. */
interface EmissionFactorSlotRule {
    fun check(item: EmissionFactorSlot): MutableList<String>
}

data class EmissionFactorSlotPinnedGuardRule(var limit: Double) : EmissionFactorSlotRule {
    override fun check(item: EmissionFactorSlot): MutableList<String> {
        if (item.pinned && item.fuelLiters > limit) {
            return mutableListOf<String>("fuel liters ${item.fuelLiters} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorSlotCodeLengthRule(var maxLength: Int) : EmissionFactorSlotRule {
    override fun check(item: EmissionFactorSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorSlotFuelLitersRangeRule(var min: Double, var max: Double) : EmissionFactorSlotRule {
    override fun check(item: EmissionFactorSlot): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class EmissionFactorSlotRequiredLaneRule(var message: String) : EmissionFactorSlotRule {
    override fun check(item: EmissionFactorSlot): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorSlotRequiredCodeRule(var message: String) : EmissionFactorSlotRule {
    override fun check(item: EmissionFactorSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor slot. */
data class EmissionFactorSlotValidator(var rules: MutableList<EmissionFactorSlotRule>) {
    fun validate(item: EmissionFactorSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EmissionFactorSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorSlotValidator(): EmissionFactorSlotValidator {
    return EmissionFactorSlotValidator(
        mutableListOf<EmissionFactorSlotRule>(
            EmissionFactorSlotPinnedGuardRule(9.3),
            EmissionFactorSlotCodeLengthRule(86400),
            EmissionFactorSlotFuelLitersRangeRule(4.7, 1081.2),
            EmissionFactorSlotRequiredLaneRule("emission factor slot needs an entry"),
            EmissionFactorSlotRequiredCodeRule("emission factor slot needs this field"),
        ),
    )
}
