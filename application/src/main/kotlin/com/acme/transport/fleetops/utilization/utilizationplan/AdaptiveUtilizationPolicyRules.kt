package com.acme.transport.fleetops.utilization.utilizationplan

/** A rule a adaptive utilization policy must pass. Check returns its problems, or none. */
interface AdaptiveUtilizationPolicyRule {
    fun check(item: AdaptiveUtilizationPolicy): MutableList<String>
}

data class AdaptiveUtilizationPolicyRequiredTitleRule(var message: String) : AdaptiveUtilizationPolicyRule {
    override fun check(item: AdaptiveUtilizationPolicy): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveUtilizationPolicyOdometerRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveUtilizationPolicyRule {
    override fun check(item: AdaptiveUtilizationPolicy): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveUtilizationPolicyRequiredDescriptionRule(
    var message: String,
) : AdaptiveUtilizationPolicyRule {
    override fun check(item: AdaptiveUtilizationPolicy): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveUtilizationPolicyCodePrefixRule(var prefix: String) : AdaptiveUtilizationPolicyRule {
    override fun check(item: AdaptiveUtilizationPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive utilization policy. */
data class AdaptiveUtilizationPolicyValidator(var rules: MutableList<AdaptiveUtilizationPolicyRule>) {
    fun validate(item: AdaptiveUtilizationPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveUtilizationPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveUtilizationPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveUtilizationPolicyValidator(): AdaptiveUtilizationPolicyValidator {
    return AdaptiveUtilizationPolicyValidator(
        mutableListOf<AdaptiveUtilizationPolicyRule>(
            AdaptiveUtilizationPolicyRequiredTitleRule("adaptive utilization policy needs this field"),
            AdaptiveUtilizationPolicyOdometerRangeRule(64, 255),
            AdaptiveUtilizationPolicyRequiredDescriptionRule("adaptive utilization policy needs this field"),
            AdaptiveUtilizationPolicyCodePrefixRule("REF"),
        ),
    )
}
