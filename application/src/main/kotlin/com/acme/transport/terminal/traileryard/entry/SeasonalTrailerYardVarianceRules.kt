package com.acme.transport.terminal.traileryard.entry

/** A rule a seasonal trailer yard variance must pass. Check returns its problems, or none. */
interface SeasonalTrailerYardVarianceRule {
    fun check(item: SeasonalTrailerYardVariance): MutableList<String>
}

data class SeasonalTrailerYardVarianceDriverIdLengthRule(var maxLength: Int) : SeasonalTrailerYardVarianceRule {
    override fun check(item: SeasonalTrailerYardVariance): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardVarianceRequiredDriverIdRule(
    var message: String,
) : SeasonalTrailerYardVarianceRule {
    override fun check(item: SeasonalTrailerYardVariance): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardVarianceAmountRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalTrailerYardVarianceRule {
    override fun check(item: SeasonalTrailerYardVariance): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardVarianceNameLengthRule(var maxLength: Int) : SeasonalTrailerYardVarianceRule {
    override fun check(item: SeasonalTrailerYardVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardVarianceRequiredCodeRule(var message: String) : SeasonalTrailerYardVarianceRule {
    override fun check(item: SeasonalTrailerYardVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardVarianceCodePrefixRule(var prefix: String) : SeasonalTrailerYardVarianceRule {
    override fun check(item: SeasonalTrailerYardVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal trailer yard variance. */
data class SeasonalTrailerYardVarianceValidator(var rules: MutableList<SeasonalTrailerYardVarianceRule>) {
    fun validate(item: SeasonalTrailerYardVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalTrailerYardVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTrailerYardVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTrailerYardVarianceValidator(): SeasonalTrailerYardVarianceValidator {
    return SeasonalTrailerYardVarianceValidator(
        mutableListOf<SeasonalTrailerYardVarianceRule>(
            SeasonalTrailerYardVarianceDriverIdLengthRule(80),
            SeasonalTrailerYardVarianceRequiredDriverIdRule("seasonal trailer yard variance needs an entry"),
            SeasonalTrailerYardVarianceAmountRangeRule(6.0, 217.9),
            SeasonalTrailerYardVarianceNameLengthRule(80),
            SeasonalTrailerYardVarianceRequiredCodeRule("seasonal trailer yard variance needs an entry"),
            SeasonalTrailerYardVarianceCodePrefixRule("ACM"),
        ),
    )
}
