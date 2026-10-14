package com.acme.transport.emissions.fuelburn.metric

/** A rule a tiered fuel burn slot must pass. Check returns its problems, or none. */
interface TieredFuelBurnSlotRule {
    fun check(item: TieredFuelBurnSlot): MutableList<String>
}

data class TieredFuelBurnSlotChecksumLengthRule(var maxLength: Int) : TieredFuelBurnSlotRule {
    override fun check(item: TieredFuelBurnSlot): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelBurnSlotRequiredDockRule(var message: String) : TieredFuelBurnSlotRule {
    override fun check(item: TieredFuelBurnSlot): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelBurnSlotRequiredNameRule(var message: String) : TieredFuelBurnSlotRule {
    override fun check(item: TieredFuelBurnSlot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelBurnSlotRequiredChecksumRule(var message: String) : TieredFuelBurnSlotRule {
    override fun check(item: TieredFuelBurnSlot): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered fuel burn slot. */
data class TieredFuelBurnSlotValidator(var rules: MutableList<TieredFuelBurnSlotRule>) {
    fun validate(item: TieredFuelBurnSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredFuelBurnSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredFuelBurnSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredFuelBurnSlotValidator(): TieredFuelBurnSlotValidator {
    return TieredFuelBurnSlotValidator(
        mutableListOf<TieredFuelBurnSlotRule>(
            TieredFuelBurnSlotChecksumLengthRule(64),
            TieredFuelBurnSlotRequiredDockRule("tiered fuel burn slot needs an entry"),
            TieredFuelBurnSlotRequiredNameRule("tiered fuel burn slot needs a value"),
            TieredFuelBurnSlotRequiredChecksumRule("tiered fuel burn slot needs this field"),
        ),
    )
}
