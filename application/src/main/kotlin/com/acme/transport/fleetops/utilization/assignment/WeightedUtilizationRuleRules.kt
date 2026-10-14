package com.acme.transport.fleetops.utilization.assignment

/** A rule a weighted utilization rule must pass. Check returns its problems, or none. */
interface WeightedUtilizationRuleRule {
    fun check(item: WeightedUtilizationRule): MutableList<String>
}

data class WeightedUtilizationRuleCodeLengthRule(var maxLength: Int) : WeightedUtilizationRuleRule {
    override fun check(item: WeightedUtilizationRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedUtilizationRuleCodePrefixRule(var prefix: String) : WeightedUtilizationRuleRule {
    override fun check(item: WeightedUtilizationRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedUtilizationRuleRequiredPhoneRule(var message: String) : WeightedUtilizationRuleRule {
    override fun check(item: WeightedUtilizationRule): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedUtilizationRulePhoneLengthRule(var maxLength: Int) : WeightedUtilizationRuleRule {
    override fun check(item: WeightedUtilizationRule): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted utilization rule. */
data class WeightedUtilizationRuleValidator(var rules: MutableList<WeightedUtilizationRuleRule>) {
    fun validate(item: WeightedUtilizationRule): MutableList<String> {
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

    fun requireValid(item: WeightedUtilizationRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedUtilizationRule>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedUtilizationRuleValidator(): WeightedUtilizationRuleValidator {
    return WeightedUtilizationRuleValidator(
        mutableListOf<WeightedUtilizationRuleRule>(
            WeightedUtilizationRuleCodeLengthRule(80),
            WeightedUtilizationRuleCodePrefixRule("X"),
            WeightedUtilizationRuleRequiredPhoneRule("weighted utilization rule needs an entry"),
            WeightedUtilizationRulePhoneLengthRule(86400),
        ),
    )
}
