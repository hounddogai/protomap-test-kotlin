package com.acme.transport.customs.tariff.score

/** A rule a tariff score must pass. Check returns its problems, or none. */
interface TariffScoreRule {
    fun check(item: TariffScore): MutableList<String>
}

data class TariffScoreCountryLengthRule(var maxLength: Int) : TariffScoreRule {
    override fun check(item: TariffScore): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffScoreCodePrefixRule(var prefix: String) : TariffScoreRule {
    override fun check(item: TariffScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TariffScoreScoreRangeRule(var min: Double, var max: Double) : TariffScoreRule {
    override fun check(item: TariffScore): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff score. */
data class TariffScoreValidator(var rules: MutableList<TariffScoreRule>) {
    fun validate(item: TariffScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TariffScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffScore>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffScoreValidator(): TariffScoreValidator {
    return TariffScoreValidator(
        mutableListOf<TariffScoreRule>(
            TariffScoreCountryLengthRule(80),
            TariffScoreCodePrefixRule("REF"),
            TariffScoreScoreRangeRule(3.6, 4594.5),
        ),
    )
}
