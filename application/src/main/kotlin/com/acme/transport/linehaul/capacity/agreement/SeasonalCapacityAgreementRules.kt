package com.acme.transport.linehaul.capacity.agreement

/** A rule a seasonal capacity agreement must pass. Check returns its problems, or none. */
interface SeasonalCapacityAgreementRule {
    fun check(item: SeasonalCapacityAgreement): MutableList<String>
}

data class SeasonalCapacityAgreementCodeLengthRule(var maxLength: Int) : SeasonalCapacityAgreementRule {
    override fun check(item: SeasonalCapacityAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCapacityAgreementCodePrefixRule(var prefix: String) : SeasonalCapacityAgreementRule {
    override fun check(item: SeasonalCapacityAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCapacityAgreementCostRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalCapacityAgreementRule {
    override fun check(item: SeasonalCapacityAgreement): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCapacityAgreementStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalCapacityAgreementRule {
    override fun check(item: SeasonalCapacityAgreement): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class SeasonalCapacityAgreementRequiredNameRule(var message: String) : SeasonalCapacityAgreementRule {
    override fun check(item: SeasonalCapacityAgreement): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal capacity agreement. */
data class SeasonalCapacityAgreementValidator(var rules: MutableList<SeasonalCapacityAgreementRule>) {
    fun validate(item: SeasonalCapacityAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalCapacityAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalCapacityAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalCapacityAgreementValidator(): SeasonalCapacityAgreementValidator {
    return SeasonalCapacityAgreementValidator(
        mutableListOf<SeasonalCapacityAgreementRule>(
            SeasonalCapacityAgreementCodeLengthRule(32),
            SeasonalCapacityAgreementCodePrefixRule("REF"),
            SeasonalCapacityAgreementCostRangeRule(1.2, 305.5),
            SeasonalCapacityAgreementStartsAtRangeRule(80, 86400),
            SeasonalCapacityAgreementRequiredNameRule("seasonal capacity agreement needs an entry"),
        ),
    )
}
