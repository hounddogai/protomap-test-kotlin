package com.acme.transport.emissions.emissionfactor.preference

/** A rule a emission factor policy must pass. Check returns its problems, or none. */
interface EmissionFactorPolicyRule {
    fun check(item: EmissionFactorPolicy): MutableList<String>
}

data class EmissionFactorPolicyRequiredLabelRule(var message: String) : EmissionFactorPolicyRule {
    override fun check(item: EmissionFactorPolicy): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorPolicyCodePrefixRule(var prefix: String) : EmissionFactorPolicyRule {
    override fun check(item: EmissionFactorPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorPolicyThresholdRangeRule(var min: Double, var max: Double) : EmissionFactorPolicyRule {
    override fun check(item: EmissionFactorPolicy): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class EmissionFactorPolicyCodeLengthRule(var maxLength: Int) : EmissionFactorPolicyRule {
    override fun check(item: EmissionFactorPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorPolicyLabelLengthRule(var maxLength: Int) : EmissionFactorPolicyRule {
    override fun check(item: EmissionFactorPolicy): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorPolicyRateRangeRule(var min: Double, var max: Double) : EmissionFactorPolicyRule {
    override fun check(item: EmissionFactorPolicy): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor policy. */
data class EmissionFactorPolicyValidator(var rules: MutableList<EmissionFactorPolicyRule>) {
    fun validate(item: EmissionFactorPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EmissionFactorPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorPolicyValidator(): EmissionFactorPolicyValidator {
    return EmissionFactorPolicyValidator(
        mutableListOf<EmissionFactorPolicyRule>(
            EmissionFactorPolicyRequiredLabelRule("emission factor policy needs this field"),
            EmissionFactorPolicyCodePrefixRule("REF"),
            EmissionFactorPolicyThresholdRangeRule(0.7, 2651.6),
            EmissionFactorPolicyCodeLengthRule(255),
            EmissionFactorPolicyLabelLengthRule(120),
            EmissionFactorPolicyRateRangeRule(9.0, 2079.6),
        ),
    )
}
