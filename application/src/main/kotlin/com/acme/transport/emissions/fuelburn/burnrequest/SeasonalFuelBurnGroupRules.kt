package com.acme.transport.emissions.fuelburn.burnrequest

import com.acme.transport.kit.text.normalize

/** A rule a seasonal fuel burn group must pass. Check returns its problems, or none. */
interface SeasonalFuelBurnGroupRule {
    fun check(item: SeasonalFuelBurnGroup): MutableList<String>
}

data class SeasonalFuelBurnGroupRequiredExternalRefRule(var message: String) : SeasonalFuelBurnGroupRule {
    override fun check(item: SeasonalFuelBurnGroup): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFuelBurnGroupMaximumRangeRule(var min: Double, var max: Double) : SeasonalFuelBurnGroupRule {
    override fun check(item: SeasonalFuelBurnGroup): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFuelBurnGroupRequiredCodeRule(var message: String) : SeasonalFuelBurnGroupRule {
    override fun check(item: SeasonalFuelBurnGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFuelBurnGroupAllowedRouteCodeRule(
    var allowed: MutableList<String>,
) : SeasonalFuelBurnGroupRule {
    override fun check(item: SeasonalFuelBurnGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is expired")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFuelBurnGroupExternalRefLengthRule(var maxLength: Int) : SeasonalFuelBurnGroupRule {
    override fun check(item: SeasonalFuelBurnGroup): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFuelBurnGroupSummaryLengthRule(var maxLength: Int) : SeasonalFuelBurnGroupRule {
    override fun check(item: SeasonalFuelBurnGroup): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal fuel burn group. */
data class SeasonalFuelBurnGroupValidator(var rules: MutableList<SeasonalFuelBurnGroupRule>) {
    fun validate(item: SeasonalFuelBurnGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalFuelBurnGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalFuelBurnGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalFuelBurnGroupValidator(): SeasonalFuelBurnGroupValidator {
    return SeasonalFuelBurnGroupValidator(
        mutableListOf<SeasonalFuelBurnGroupRule>(
            SeasonalFuelBurnGroupRequiredExternalRefRule("seasonal fuel burn group needs a value"),
            SeasonalFuelBurnGroupMaximumRangeRule(6.5, 81.2),
            SeasonalFuelBurnGroupRequiredCodeRule("seasonal fuel burn group needs an entry"),
            SeasonalFuelBurnGroupAllowedRouteCodeRule(mutableListOf<String>("legacy")),
            SeasonalFuelBurnGroupExternalRefLengthRule(255),
            SeasonalFuelBurnGroupSummaryLengthRule(255),
        ),
    )
}
