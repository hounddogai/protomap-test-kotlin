package com.acme.transport.emissions.emissionfactor.assignment

/** A rule a adaptive emission factor request must pass. Check returns its problems, or none. */
interface AdaptiveEmissionFactorRequestRule {
    fun check(item: AdaptiveEmissionFactorRequest): MutableList<String>
}

data class AdaptiveEmissionFactorRequestActiveGuardRule(var limit: Int) : AdaptiveEmissionFactorRequestRule {
    override fun check(item: AdaptiveEmissionFactorRequest): MutableList<String> {
        if (item.active && item.revision > limit) {
            return mutableListOf<String>("revision ${item.revision} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorRequestCodePrefixRule(var prefix: String) : AdaptiveEmissionFactorRequestRule {
    override fun check(item: AdaptiveEmissionFactorRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorRequestRequiredTrailerIdRule(
    var message: String,
) : AdaptiveEmissionFactorRequestRule {
    override fun check(item: AdaptiveEmissionFactorRequest): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorRequestRequiredTitleRule(
    var message: String,
) : AdaptiveEmissionFactorRequestRule {
    override fun check(item: AdaptiveEmissionFactorRequest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorRequestMinimumRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveEmissionFactorRequestRule {
    override fun check(item: AdaptiveEmissionFactorRequest): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive emission factor request. */
data class AdaptiveEmissionFactorRequestValidator(var rules: MutableList<AdaptiveEmissionFactorRequestRule>) {
    fun validate(item: AdaptiveEmissionFactorRequest): MutableList<String> {
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

    fun requireValid(item: AdaptiveEmissionFactorRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveEmissionFactorRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveEmissionFactorRequestValidator(): AdaptiveEmissionFactorRequestValidator {
    return AdaptiveEmissionFactorRequestValidator(
        mutableListOf<AdaptiveEmissionFactorRequestRule>(
            AdaptiveEmissionFactorRequestActiveGuardRule(86400),
            AdaptiveEmissionFactorRequestCodePrefixRule("OPS"),
            AdaptiveEmissionFactorRequestRequiredTrailerIdRule("adaptive emission factor request needs a value"),
            AdaptiveEmissionFactorRequestRequiredTitleRule("adaptive emission factor request needs a value"),
            AdaptiveEmissionFactorRequestMinimumRangeRule(0.8, 3040.4),
        ),
    )
}
