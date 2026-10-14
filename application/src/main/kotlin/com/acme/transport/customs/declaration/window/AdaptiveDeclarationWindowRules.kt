package com.acme.transport.customs.declaration.window

/** A rule a adaptive declaration window must pass. Check returns its problems, or none. */
interface AdaptiveDeclarationWindowRule {
    fun check(item: AdaptiveDeclarationWindow): MutableList<String>
}

data class AdaptiveDeclarationWindowDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveDeclarationWindowRule {
    override fun check(item: AdaptiveDeclarationWindow): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class AdaptiveDeclarationWindowVehicleIdLengthRule(var maxLength: Int) : AdaptiveDeclarationWindowRule {
    override fun check(item: AdaptiveDeclarationWindow): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDeclarationWindowCountryLengthRule(var maxLength: Int) : AdaptiveDeclarationWindowRule {
    override fun check(item: AdaptiveDeclarationWindow): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive declaration window. */
data class AdaptiveDeclarationWindowValidator(var rules: MutableList<AdaptiveDeclarationWindowRule>) {
    fun validate(item: AdaptiveDeclarationWindow): MutableList<String> {
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

    fun requireValid(item: AdaptiveDeclarationWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveDeclarationWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveDeclarationWindowValidator(): AdaptiveDeclarationWindowValidator {
    return AdaptiveDeclarationWindowValidator(
        mutableListOf<AdaptiveDeclarationWindowRule>(
            AdaptiveDeclarationWindowDurationMinutesRangeRule(255, 1000),
            AdaptiveDeclarationWindowVehicleIdLengthRule(80),
            AdaptiveDeclarationWindowCountryLengthRule(32),
        ),
    )
}
