package com.acme.transport.emissions.emissionfactor.target

/** A rule a fallback emission factor request must pass. Check returns its problems, or none. */
interface FallbackEmissionFactorRequestRule {
    fun check(item: FallbackEmissionFactorRequest): MutableList<String>
}

data class FallbackEmissionFactorRequestDiscountRangeRule(
    var min: Double,
    var max: Double,
) : FallbackEmissionFactorRequestRule {
    override fun check(item: FallbackEmissionFactorRequest): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEmissionFactorRequestVersionRangeRule(
    var min: Int,
    var max: Int,
) : FallbackEmissionFactorRequestRule {
    override fun check(item: FallbackEmissionFactorRequest): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEmissionFactorRequestRequiredTitleRule(
    var message: String,
) : FallbackEmissionFactorRequestRule {
    override fun check(item: FallbackEmissionFactorRequest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback emission factor request. */
data class FallbackEmissionFactorRequestValidator(var rules: MutableList<FallbackEmissionFactorRequestRule>) {
    fun validate(item: FallbackEmissionFactorRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackEmissionFactorRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackEmissionFactorRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackEmissionFactorRequestValidator(): FallbackEmissionFactorRequestValidator {
    return FallbackEmissionFactorRequestValidator(
        mutableListOf<FallbackEmissionFactorRequestRule>(
            FallbackEmissionFactorRequestDiscountRangeRule(5.2, 4870.3),
            FallbackEmissionFactorRequestVersionRangeRule(1000, 1000),
            FallbackEmissionFactorRequestRequiredTitleRule("fallback emission factor request needs this field"),
        ),
    )
}
