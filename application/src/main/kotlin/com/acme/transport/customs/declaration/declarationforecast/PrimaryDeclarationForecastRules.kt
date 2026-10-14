package com.acme.transport.customs.declaration.declarationforecast

/** A rule a primary declaration forecast must pass. Check returns its problems, or none. */
interface PrimaryDeclarationForecastRule {
    fun check(item: PrimaryDeclarationForecast): MutableList<String>
}

data class PrimaryDeclarationForecastNotesLengthRule(var maxLength: Int) : PrimaryDeclarationForecastRule {
    override fun check(item: PrimaryDeclarationForecast): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDeclarationForecastRateRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryDeclarationForecastRule {
    override fun check(item: PrimaryDeclarationForecast): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class PrimaryDeclarationForecastCodePrefixRule(var prefix: String) : PrimaryDeclarationForecastRule {
    override fun check(item: PrimaryDeclarationForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary declaration forecast. */
data class PrimaryDeclarationForecastValidator(var rules: MutableList<PrimaryDeclarationForecastRule>) {
    fun validate(item: PrimaryDeclarationForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryDeclarationForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryDeclarationForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryDeclarationForecastValidator(): PrimaryDeclarationForecastValidator {
    return PrimaryDeclarationForecastValidator(
        mutableListOf<PrimaryDeclarationForecastRule>(
            PrimaryDeclarationForecastNotesLengthRule(120),
            PrimaryDeclarationForecastRateRangeRule(7.5, 262.5),
            PrimaryDeclarationForecastCodePrefixRule("ACM"),
        ),
    )
}
