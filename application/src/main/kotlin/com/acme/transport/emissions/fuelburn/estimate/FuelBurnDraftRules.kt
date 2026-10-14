package com.acme.transport.emissions.fuelburn.estimate

/** A rule a fuel burn draft must pass. Check returns its problems, or none. */
interface FuelBurnDraftRule {
    fun check(item: FuelBurnDraft): MutableList<String>
}

data class FuelBurnDraftCodePrefixRule(var prefix: String) : FuelBurnDraftRule {
    override fun check(item: FuelBurnDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnDraftRequiredTrailerIdRule(var message: String) : FuelBurnDraftRule {
    override fun check(item: FuelBurnDraft): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnDraftMinimumRangeRule(var min: Double, var max: Double) : FuelBurnDraftRule {
    override fun check(item: FuelBurnDraft): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class FuelBurnDraftTrailerIdLengthRule(var maxLength: Int) : FuelBurnDraftRule {
    override fun check(item: FuelBurnDraft): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn draft. */
data class FuelBurnDraftValidator(var rules: MutableList<FuelBurnDraftRule>) {
    fun validate(item: FuelBurnDraft): MutableList<String> {
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

    fun requireValid(item: FuelBurnDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnDraftValidator(): FuelBurnDraftValidator {
    return FuelBurnDraftValidator(
        mutableListOf<FuelBurnDraftRule>(
            FuelBurnDraftCodePrefixRule("OPS"),
            FuelBurnDraftRequiredTrailerIdRule("fuel burn draft needs an entry"),
            FuelBurnDraftMinimumRangeRule(2.4, 3394.5),
            FuelBurnDraftTrailerIdLengthRule(86400),
        ),
    )
}
