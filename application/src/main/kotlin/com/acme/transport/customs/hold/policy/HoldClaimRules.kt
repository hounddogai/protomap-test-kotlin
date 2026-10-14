package com.acme.transport.customs.hold.policy

/** A rule a hold claim must pass. Check returns its problems, or none. */
interface HoldClaimRule {
    fun check(item: HoldClaim): MutableList<String>
}

data class HoldClaimCodePrefixRule(var prefix: String) : HoldClaimRule {
    override fun check(item: HoldClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class HoldClaimLabelLengthRule(var maxLength: Int) : HoldClaimRule {
    override fun check(item: HoldClaim): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldClaimCodeLengthRule(var maxLength: Int) : HoldClaimRule {
    override fun check(item: HoldClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldClaimCeilingRangeRule(var min: Double, var max: Double) : HoldClaimRule {
    override fun check(item: HoldClaim): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class HoldClaimRequiredLabelRule(var message: String) : HoldClaimRule {
    override fun check(item: HoldClaim): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldClaimRequiredChannelRule(var message: String) : HoldClaimRule {
    override fun check(item: HoldClaim): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold claim. */
data class HoldClaimValidator(var rules: MutableList<HoldClaimRule>) {
    fun validate(item: HoldClaim): MutableList<String> {
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

    fun requireValid(item: HoldClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldClaimValidator(): HoldClaimValidator {
    return HoldClaimValidator(
        mutableListOf<HoldClaimRule>(
            HoldClaimCodePrefixRule("X"),
            HoldClaimLabelLengthRule(1000),
            HoldClaimCodeLengthRule(120),
            HoldClaimCeilingRangeRule(7.3, 743.1),
            HoldClaimRequiredLabelRule("hold claim needs this field"),
            HoldClaimRequiredChannelRule("hold claim needs an entry"),
        ),
    )
}
