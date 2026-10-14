package com.acme.transport.lastmile.redelivery.policy

/** A rule a fallback redelivery policy must pass. Check returns its problems, or none. */
interface FallbackRedeliveryPolicyRule {
    fun check(item: FallbackRedeliveryPolicy): MutableList<String>
}

data class FallbackRedeliveryPolicyRequiredShiftCodeRule(var message: String) : FallbackRedeliveryPolicyRule {
    override fun check(item: FallbackRedeliveryPolicy): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRedeliveryPolicyShiftCodeLengthRule(var maxLength: Int) : FallbackRedeliveryPolicyRule {
    override fun check(item: FallbackRedeliveryPolicy): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRedeliveryPolicyRevisionRangeRule(
    var min: Int,
    var max: Int,
) : FallbackRedeliveryPolicyRule {
    override fun check(item: FallbackRedeliveryPolicy): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback redelivery policy. */
data class FallbackRedeliveryPolicyValidator(var rules: MutableList<FallbackRedeliveryPolicyRule>) {
    fun validate(item: FallbackRedeliveryPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackRedeliveryPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRedeliveryPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRedeliveryPolicyValidator(): FallbackRedeliveryPolicyValidator {
    return FallbackRedeliveryPolicyValidator(
        mutableListOf<FallbackRedeliveryPolicyRule>(
            FallbackRedeliveryPolicyRequiredShiftCodeRule("fallback redelivery policy needs this field"),
            FallbackRedeliveryPolicyShiftCodeLengthRule(64),
            FallbackRedeliveryPolicyRevisionRangeRule(16, 3600),
        ),
    )
}
