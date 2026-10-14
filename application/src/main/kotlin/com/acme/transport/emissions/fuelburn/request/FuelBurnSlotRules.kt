package com.acme.transport.emissions.fuelburn.request

/** A rule a fuel burn slot must pass. Check returns its problems, or none. */
interface FuelBurnSlotRule {
    fun check(item: FuelBurnSlot): MutableList<String>
}

data class FuelBurnSlotCodePrefixRule(var prefix: String) : FuelBurnSlotRule {
    override fun check(item: FuelBurnSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnSlotNameLengthRule(var maxLength: Int) : FuelBurnSlotRule {
    override fun check(item: FuelBurnSlot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnSlotCodeLengthRule(var maxLength: Int) : FuelBurnSlotRule {
    override fun check(item: FuelBurnSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnSlotRequiredNameRule(var message: String) : FuelBurnSlotRule {
    override fun check(item: FuelBurnSlot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnSlotDepotLengthRule(var maxLength: Int) : FuelBurnSlotRule {
    override fun check(item: FuelBurnSlot): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn slot. */
data class FuelBurnSlotValidator(var rules: MutableList<FuelBurnSlotRule>) {
    fun validate(item: FuelBurnSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelBurnSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnSlotValidator(): FuelBurnSlotValidator {
    return FuelBurnSlotValidator(
        mutableListOf<FuelBurnSlotRule>(
            FuelBurnSlotCodePrefixRule("REF"),
            FuelBurnSlotNameLengthRule(86400),
            FuelBurnSlotCodeLengthRule(16),
            FuelBurnSlotRequiredNameRule("fuel burn slot needs a value"),
            FuelBurnSlotDepotLengthRule(86400),
        ),
    )
}
