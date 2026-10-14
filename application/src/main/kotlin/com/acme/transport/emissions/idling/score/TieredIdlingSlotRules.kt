package com.acme.transport.emissions.idling.score

/** A rule a tiered idling slot must pass. Check returns its problems, or none. */
interface TieredIdlingSlotRule {
    fun check(item: TieredIdlingSlot): MutableList<String>
}

data class TieredIdlingSlotFuelLitersRangeRule(var min: Double, var max: Double) : TieredIdlingSlotRule {
    override fun check(item: TieredIdlingSlot): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredIdlingSlotRequiredUnitRule(var message: String) : TieredIdlingSlotRule {
    override fun check(item: TieredIdlingSlot): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredIdlingSlotUnitLengthRule(var maxLength: Int) : TieredIdlingSlotRule {
    override fun check(item: TieredIdlingSlot): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered idling slot. */
data class TieredIdlingSlotValidator(var rules: MutableList<TieredIdlingSlotRule>) {
    fun validate(item: TieredIdlingSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredIdlingSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredIdlingSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredIdlingSlotValidator(): TieredIdlingSlotValidator {
    return TieredIdlingSlotValidator(
        mutableListOf<TieredIdlingSlotRule>(
            TieredIdlingSlotFuelLitersRangeRule(3.7, 2173.6),
            TieredIdlingSlotRequiredUnitRule("tiered idling slot needs this field"),
            TieredIdlingSlotUnitLengthRule(3600),
        ),
    )
}
