package com.acme.transport.emissions.fuelburn.recorditem

/** A rule a strict fuel burn group must pass. Check returns its problems, or none. */
interface StrictFuelBurnGroupRule {
    fun check(item: StrictFuelBurnGroup): MutableList<String>
}

data class StrictFuelBurnGroupRequiredNameRule(var message: String) : StrictFuelBurnGroupRule {
    override fun check(item: StrictFuelBurnGroup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictFuelBurnGroupPalletsRangeRule(var min: Int, var max: Int) : StrictFuelBurnGroupRule {
    override fun check(item: StrictFuelBurnGroup): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictFuelBurnGroupCodePrefixRule(var prefix: String) : StrictFuelBurnGroupRule {
    override fun check(item: StrictFuelBurnGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictFuelBurnGroupUnitLengthRule(var maxLength: Int) : StrictFuelBurnGroupRule {
    override fun check(item: StrictFuelBurnGroup): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictFuelBurnGroupRequiredCodeRule(var message: String) : StrictFuelBurnGroupRule {
    override fun check(item: StrictFuelBurnGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictFuelBurnGroupLabelLengthRule(var maxLength: Int) : StrictFuelBurnGroupRule {
    override fun check(item: StrictFuelBurnGroup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict fuel burn group. */
data class StrictFuelBurnGroupValidator(var rules: MutableList<StrictFuelBurnGroupRule>) {
    fun validate(item: StrictFuelBurnGroup): MutableList<String> {
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

    fun requireValid(item: StrictFuelBurnGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictFuelBurnGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictFuelBurnGroupValidator(): StrictFuelBurnGroupValidator {
    return StrictFuelBurnGroupValidator(
        mutableListOf<StrictFuelBurnGroupRule>(
            StrictFuelBurnGroupRequiredNameRule("strict fuel burn group needs an entry"),
            StrictFuelBurnGroupPalletsRangeRule(64, 80),
            StrictFuelBurnGroupCodePrefixRule("REF"),
            StrictFuelBurnGroupUnitLengthRule(80),
            StrictFuelBurnGroupRequiredCodeRule("strict fuel burn group needs this field"),
            StrictFuelBurnGroupLabelLengthRule(1000),
        ),
    )
}
