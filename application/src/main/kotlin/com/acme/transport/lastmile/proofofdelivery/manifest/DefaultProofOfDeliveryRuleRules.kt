package com.acme.transport.lastmile.proofofdelivery.manifest

/** A rule a default proof of delivery rule must pass. Check returns its problems, or none. */
interface DefaultProofOfDeliveryRuleRule {
    fun check(item: DefaultProofOfDeliveryRule): MutableList<String>
}

data class DefaultProofOfDeliveryRuleRequiredCodeRule(var message: String) : DefaultProofOfDeliveryRuleRule {
    override fun check(item: DefaultProofOfDeliveryRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultProofOfDeliveryRuleRequiredNameRule(var message: String) : DefaultProofOfDeliveryRuleRule {
    override fun check(item: DefaultProofOfDeliveryRule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultProofOfDeliveryRuleCapacityRangeRule(
    var min: Int,
    var max: Int,
) : DefaultProofOfDeliveryRuleRule {
    override fun check(item: DefaultProofOfDeliveryRule): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class DefaultProofOfDeliveryRuleNameLengthRule(var maxLength: Int) : DefaultProofOfDeliveryRuleRule {
    override fun check(item: DefaultProofOfDeliveryRule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultProofOfDeliveryRuleRequiredLocaleRule(var message: String) : DefaultProofOfDeliveryRuleRule {
    override fun check(item: DefaultProofOfDeliveryRule): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default proof of delivery rule. */
data class DefaultProofOfDeliveryRuleValidator(var rules: MutableList<DefaultProofOfDeliveryRuleRule>) {
    fun validate(item: DefaultProofOfDeliveryRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultProofOfDeliveryRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultProofOfDeliveryRule>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultProofOfDeliveryRuleValidator(): DefaultProofOfDeliveryRuleValidator {
    return DefaultProofOfDeliveryRuleValidator(
        mutableListOf<DefaultProofOfDeliveryRuleRule>(
            DefaultProofOfDeliveryRuleRequiredCodeRule("default proof of delivery rule needs this field"),
            DefaultProofOfDeliveryRuleRequiredNameRule("default proof of delivery rule needs this field"),
            DefaultProofOfDeliveryRuleCapacityRangeRule(255, 255),
            DefaultProofOfDeliveryRuleNameLengthRule(3600),
            DefaultProofOfDeliveryRuleRequiredLocaleRule("default proof of delivery rule needs this field"),
        ),
    )
}
