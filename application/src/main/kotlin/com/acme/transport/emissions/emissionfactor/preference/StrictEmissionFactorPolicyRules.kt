package com.acme.transport.emissions.emissionfactor.preference

/** A rule a strict emission factor policy must pass. Check returns its problems, or none. */
interface StrictEmissionFactorPolicyRule {
    fun check(item: StrictEmissionFactorPolicy): MutableList<String>
}

data class StrictEmissionFactorPolicyUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : StrictEmissionFactorPolicyRule {
    override fun check(item: StrictEmissionFactorPolicy): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class StrictEmissionFactorPolicyCodeLengthRule(var maxLength: Int) : StrictEmissionFactorPolicyRule {
    override fun check(item: StrictEmissionFactorPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictEmissionFactorPolicyRequiredCodeRule(var message: String) : StrictEmissionFactorPolicyRule {
    override fun check(item: StrictEmissionFactorPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictEmissionFactorPolicyRequiredTrailerIdRule(
    var message: String,
) : StrictEmissionFactorPolicyRule {
    override fun check(item: StrictEmissionFactorPolicy): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictEmissionFactorPolicyNameLengthRule(var maxLength: Int) : StrictEmissionFactorPolicyRule {
    override fun check(item: StrictEmissionFactorPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict emission factor policy. */
data class StrictEmissionFactorPolicyValidator(var rules: MutableList<StrictEmissionFactorPolicyRule>) {
    fun validate(item: StrictEmissionFactorPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictEmissionFactorPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictEmissionFactorPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictEmissionFactorPolicyValidator(): StrictEmissionFactorPolicyValidator {
    return StrictEmissionFactorPolicyValidator(
        mutableListOf<StrictEmissionFactorPolicyRule>(
            StrictEmissionFactorPolicyUpdatedAtRangeRule(64, 120),
            StrictEmissionFactorPolicyCodeLengthRule(16),
            StrictEmissionFactorPolicyRequiredCodeRule("strict emission factor policy needs an entry"),
            StrictEmissionFactorPolicyRequiredTrailerIdRule("strict emission factor policy needs a value"),
            StrictEmissionFactorPolicyNameLengthRule(120),
        ),
    )
}
