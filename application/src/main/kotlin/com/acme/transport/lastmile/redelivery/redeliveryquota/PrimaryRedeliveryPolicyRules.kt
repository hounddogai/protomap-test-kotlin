package com.acme.transport.lastmile.redelivery.redeliveryquota

/** A rule a primary redelivery policy must pass. Check returns its problems, or none. */
interface PrimaryRedeliveryPolicyRule {
    fun check(item: PrimaryRedeliveryPolicy): MutableList<String>
}

data class PrimaryRedeliveryPolicyAmountRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryRedeliveryPolicyRule {
    override fun check(item: PrimaryRedeliveryPolicy): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRedeliveryPolicyCodeLengthRule(var maxLength: Int) : PrimaryRedeliveryPolicyRule {
    override fun check(item: PrimaryRedeliveryPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRedeliveryPolicyCarrierLengthRule(var maxLength: Int) : PrimaryRedeliveryPolicyRule {
    override fun check(item: PrimaryRedeliveryPolicy): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRedeliveryPolicyRequiredLabelRule(var message: String) : PrimaryRedeliveryPolicyRule {
    override fun check(item: PrimaryRedeliveryPolicy): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary redelivery policy. */
data class PrimaryRedeliveryPolicyValidator(var rules: MutableList<PrimaryRedeliveryPolicyRule>) {
    fun validate(item: PrimaryRedeliveryPolicy): MutableList<String> {
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

    fun requireValid(item: PrimaryRedeliveryPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryRedeliveryPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryRedeliveryPolicyValidator(): PrimaryRedeliveryPolicyValidator {
    return PrimaryRedeliveryPolicyValidator(
        mutableListOf<PrimaryRedeliveryPolicyRule>(
            PrimaryRedeliveryPolicyAmountRangeRule(7.9, 168.8),
            PrimaryRedeliveryPolicyCodeLengthRule(1000),
            PrimaryRedeliveryPolicyCarrierLengthRule(86400),
            PrimaryRedeliveryPolicyRequiredLabelRule("primary redelivery policy needs a value"),
        ),
    )
}
