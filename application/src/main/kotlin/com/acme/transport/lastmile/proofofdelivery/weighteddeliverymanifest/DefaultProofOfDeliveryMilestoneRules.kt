package com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest

/** A rule a default proof of delivery milestone must pass. Check returns its problems, or none. */
interface DefaultProofOfDeliveryMilestoneRule {
    fun check(item: DefaultProofOfDeliveryMilestone): MutableList<String>
}

data class DefaultProofOfDeliveryMilestoneRequiredDriverIdRule(
    var message: String,
) : DefaultProofOfDeliveryMilestoneRule {
    override fun check(item: DefaultProofOfDeliveryMilestone): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultProofOfDeliveryMilestoneCodePrefixRule(
    var prefix: String,
) : DefaultProofOfDeliveryMilestoneRule {
    override fun check(item: DefaultProofOfDeliveryMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultProofOfDeliveryMilestoneCodeLengthRule(
    var maxLength: Int,
) : DefaultProofOfDeliveryMilestoneRule {
    override fun check(item: DefaultProofOfDeliveryMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultProofOfDeliveryMilestoneAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : DefaultProofOfDeliveryMilestoneRule {
    override fun check(item: DefaultProofOfDeliveryMilestone): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultProofOfDeliveryMilestoneRequiredNameRule(
    var message: String,
) : DefaultProofOfDeliveryMilestoneRule {
    override fun check(item: DefaultProofOfDeliveryMilestone): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default proof of delivery milestone. */
data class DefaultProofOfDeliveryMilestoneValidator(
    var rules: MutableList<DefaultProofOfDeliveryMilestoneRule>,
) {
    fun validate(item: DefaultProofOfDeliveryMilestone): MutableList<String> {
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

    fun requireValid(item: DefaultProofOfDeliveryMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultProofOfDeliveryMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultProofOfDeliveryMilestoneValidator(): DefaultProofOfDeliveryMilestoneValidator {
    return DefaultProofOfDeliveryMilestoneValidator(
        mutableListOf<DefaultProofOfDeliveryMilestoneRule>(
            DefaultProofOfDeliveryMilestoneRequiredDriverIdRule(
                "default proof of delivery milestone needs a value",
            ),
            DefaultProofOfDeliveryMilestoneCodePrefixRule("REF"),
            DefaultProofOfDeliveryMilestoneCodeLengthRule(16),
            DefaultProofOfDeliveryMilestoneAttemptsRangeRule(64, 86400),
            DefaultProofOfDeliveryMilestoneRequiredNameRule(
                "default proof of delivery milestone needs this field",
            ),
        ),
    )
}
