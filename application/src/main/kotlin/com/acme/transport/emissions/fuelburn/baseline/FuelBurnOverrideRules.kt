package com.acme.transport.emissions.fuelburn.baseline

/** A rule a fuel burn override must pass. Check returns its problems, or none. */
interface FuelBurnOverrideRule {
    fun check(item: FuelBurnOverride): MutableList<String>
}

data class FuelBurnOverrideRequiredCodeRule(var message: String) : FuelBurnOverrideRule {
    override fun check(item: FuelBurnOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnOverrideNotesLengthRule(var maxLength: Int) : FuelBurnOverrideRule {
    override fun check(item: FuelBurnOverride): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnOverrideNameLengthRule(var maxLength: Int) : FuelBurnOverrideRule {
    override fun check(item: FuelBurnOverride): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnOverrideMaximumRangeRule(var min: Double, var max: Double) : FuelBurnOverrideRule {
    override fun check(item: FuelBurnOverride): MutableList<String> {
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

data class FuelBurnOverrideTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : FuelBurnOverrideRule {
    override fun check(item: FuelBurnOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class FuelBurnOverrideCodePrefixRule(var prefix: String) : FuelBurnOverrideRule {
    override fun check(item: FuelBurnOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn override. */
data class FuelBurnOverrideValidator(var rules: MutableList<FuelBurnOverrideRule>) {
    fun validate(item: FuelBurnOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelBurnOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnOverrideValidator(): FuelBurnOverrideValidator {
    return FuelBurnOverrideValidator(
        mutableListOf<FuelBurnOverrideRule>(
            FuelBurnOverrideRequiredCodeRule("fuel burn override needs this field"),
            FuelBurnOverrideNotesLengthRule(16),
            FuelBurnOverrideNameLengthRule(16),
            FuelBurnOverrideMaximumRangeRule(8.8, 4983.0),
            FuelBurnOverrideTagsLimitRule(80, mutableListOf<String>()),
            FuelBurnOverrideCodePrefixRule("X"),
        ),
    )
}
