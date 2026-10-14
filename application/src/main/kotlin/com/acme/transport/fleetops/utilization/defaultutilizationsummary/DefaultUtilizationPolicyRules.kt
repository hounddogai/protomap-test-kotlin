package com.acme.transport.fleetops.utilization.defaultutilizationsummary

/** A rule a default utilization policy must pass. Check returns its problems, or none. */
interface DefaultUtilizationPolicyRule {
    fun check(item: DefaultUtilizationPolicy): MutableList<String>
}

data class DefaultUtilizationPolicyLocaleLengthRule(var maxLength: Int) : DefaultUtilizationPolicyRule {
    override fun check(item: DefaultUtilizationPolicy): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultUtilizationPolicyFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : DefaultUtilizationPolicyRule {
    override fun check(item: DefaultUtilizationPolicy): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class DefaultUtilizationPolicyTitleLengthRule(var maxLength: Int) : DefaultUtilizationPolicyRule {
    override fun check(item: DefaultUtilizationPolicy): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultUtilizationPolicyRequiredLocaleRule(var message: String) : DefaultUtilizationPolicyRule {
    override fun check(item: DefaultUtilizationPolicy): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultUtilizationPolicyRequiredCodeRule(var message: String) : DefaultUtilizationPolicyRule {
    override fun check(item: DefaultUtilizationPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default utilization policy. */
data class DefaultUtilizationPolicyValidator(var rules: MutableList<DefaultUtilizationPolicyRule>) {
    fun validate(item: DefaultUtilizationPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultUtilizationPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultUtilizationPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultUtilizationPolicyValidator(): DefaultUtilizationPolicyValidator {
    return DefaultUtilizationPolicyValidator(
        mutableListOf<DefaultUtilizationPolicyRule>(
            DefaultUtilizationPolicyLocaleLengthRule(1000),
            DefaultUtilizationPolicyFloorValueRangeRule(3.3, 1261.8),
            DefaultUtilizationPolicyTitleLengthRule(86400),
            DefaultUtilizationPolicyRequiredLocaleRule("default utilization policy needs an entry"),
            DefaultUtilizationPolicyRequiredCodeRule("default utilization policy needs an entry"),
        ),
    )
}
