package com.acme.transport.customs.declaration.forecast

/** A rule a declaration forecast must pass. Check returns its problems, or none. */
interface DeclarationForecastRule {
    fun check(item: DeclarationForecast): MutableList<String>
}

data class DeclarationForecastSequenceRangeRule(var min: Int, var max: Int) : DeclarationForecastRule {
    override fun check(item: DeclarationForecast): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationForecastAttemptsRangeRule(var min: Int, var max: Int) : DeclarationForecastRule {
    override fun check(item: DeclarationForecast): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationForecastNameLengthRule(var maxLength: Int) : DeclarationForecastRule {
    override fun check(item: DeclarationForecast): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationForecastRequiredNameRule(var message: String) : DeclarationForecastRule {
    override fun check(item: DeclarationForecast): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationForecastReviewerLengthRule(var maxLength: Int) : DeclarationForecastRule {
    override fun check(item: DeclarationForecast): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration forecast. */
data class DeclarationForecastValidator(var rules: MutableList<DeclarationForecastRule>) {
    fun validate(item: DeclarationForecast): MutableList<String> {
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

    fun requireValid(item: DeclarationForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationForecastValidator(): DeclarationForecastValidator {
    return DeclarationForecastValidator(
        mutableListOf<DeclarationForecastRule>(
            DeclarationForecastSequenceRangeRule(120, 1000),
            DeclarationForecastAttemptsRangeRule(32, 64),
            DeclarationForecastNameLengthRule(1000),
            DeclarationForecastRequiredNameRule("declaration forecast needs an entry"),
            DeclarationForecastReviewerLengthRule(16),
        ),
    )
}
