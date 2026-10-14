package com.acme.transport.emissions.fuelburn.variance

/** A rule a fuel burn entry must pass. Check returns its problems, or none. */
interface FuelBurnEntryRule {
    fun check(item: FuelBurnEntry): MutableList<String>
}

data class FuelBurnEntryRequiredLocaleRule(var message: String) : FuelBurnEntryRule {
    override fun check(item: FuelBurnEntry): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnEntryAmountRangeRule(var min: Double, var max: Double) : FuelBurnEntryRule {
    override fun check(item: FuelBurnEntry): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnEntryRequiredCodeRule(var message: String) : FuelBurnEntryRule {
    override fun check(item: FuelBurnEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnEntryLabelLengthRule(var maxLength: Int) : FuelBurnEntryRule {
    override fun check(item: FuelBurnEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnEntryLocaleLengthRule(var maxLength: Int) : FuelBurnEntryRule {
    override fun check(item: FuelBurnEntry): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn entry. */
data class FuelBurnEntryValidator(var rules: MutableList<FuelBurnEntryRule>) {
    fun validate(item: FuelBurnEntry): MutableList<String> {
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

    fun requireValid(item: FuelBurnEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnEntryValidator(): FuelBurnEntryValidator {
    return FuelBurnEntryValidator(
        mutableListOf<FuelBurnEntryRule>(
            FuelBurnEntryRequiredLocaleRule("fuel burn entry needs a value"),
            FuelBurnEntryAmountRangeRule(4.4, 4229.8),
            FuelBurnEntryRequiredCodeRule("fuel burn entry needs a value"),
            FuelBurnEntryLabelLengthRule(32),
            FuelBurnEntryLocaleLengthRule(255),
        ),
    )
}
