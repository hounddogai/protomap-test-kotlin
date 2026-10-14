package com.acme.transport.emissions.fuelburn.plan

/** A rule a weighted fuel burn group must pass. Check returns its problems, or none. */
interface WeightedFuelBurnGroupRule {
    fun check(item: WeightedFuelBurnGroup): MutableList<String>
}

data class WeightedFuelBurnGroupRequiredCodeRule(var message: String) : WeightedFuelBurnGroupRule {
    override fun check(item: WeightedFuelBurnGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelBurnGroupCodePrefixRule(var prefix: String) : WeightedFuelBurnGroupRule {
    override fun check(item: WeightedFuelBurnGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelBurnGroupCodeLengthRule(var maxLength: Int) : WeightedFuelBurnGroupRule {
    override fun check(item: WeightedFuelBurnGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelBurnGroupRetriesRangeRule(var min: Int, var max: Int) : WeightedFuelBurnGroupRule {
    override fun check(item: WeightedFuelBurnGroup): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class WeightedFuelBurnGroupRequiredNotesRule(var message: String) : WeightedFuelBurnGroupRule {
    override fun check(item: WeightedFuelBurnGroup): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted fuel burn group. */
data class WeightedFuelBurnGroupValidator(var rules: MutableList<WeightedFuelBurnGroupRule>) {
    fun validate(item: WeightedFuelBurnGroup): MutableList<String> {
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

    fun requireValid(item: WeightedFuelBurnGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedFuelBurnGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedFuelBurnGroupValidator(): WeightedFuelBurnGroupValidator {
    return WeightedFuelBurnGroupValidator(
        mutableListOf<WeightedFuelBurnGroupRule>(
            WeightedFuelBurnGroupRequiredCodeRule("weighted fuel burn group needs this field"),
            WeightedFuelBurnGroupCodePrefixRule("X"),
            WeightedFuelBurnGroupCodeLengthRule(255),
            WeightedFuelBurnGroupRetriesRangeRule(32, 86400),
            WeightedFuelBurnGroupRequiredNotesRule("weighted fuel burn group needs this field"),
        ),
    )
}
