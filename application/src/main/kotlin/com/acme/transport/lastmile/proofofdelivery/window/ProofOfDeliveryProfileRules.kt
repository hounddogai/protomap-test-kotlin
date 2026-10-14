package com.acme.transport.lastmile.proofofdelivery.window

/** A rule a proof of delivery profile must pass. Check returns its problems, or none. */
interface ProofOfDeliveryProfileRule {
    fun check(item: ProofOfDeliveryProfile): MutableList<String>
}

data class ProofOfDeliveryProfileThresholdRangeRule(
    var min: Double,
    var max: Double,
) : ProofOfDeliveryProfileRule {
    override fun check(item: ProofOfDeliveryProfile): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryProfileRequiredCodeRule(var message: String) : ProofOfDeliveryProfileRule {
    override fun check(item: ProofOfDeliveryProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryProfileCodePrefixRule(var prefix: String) : ProofOfDeliveryProfileRule {
    override fun check(item: ProofOfDeliveryProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryProfileOwnerIdLengthRule(var maxLength: Int) : ProofOfDeliveryProfileRule {
    override fun check(item: ProofOfDeliveryProfile): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryProfileRequiredTitleRule(var message: String) : ProofOfDeliveryProfileRule {
    override fun check(item: ProofOfDeliveryProfile): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryProfileTitleLengthRule(var maxLength: Int) : ProofOfDeliveryProfileRule {
    override fun check(item: ProofOfDeliveryProfile): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery profile. */
data class ProofOfDeliveryProfileValidator(var rules: MutableList<ProofOfDeliveryProfileRule>) {
    fun validate(item: ProofOfDeliveryProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ProofOfDeliveryProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryProfileValidator(): ProofOfDeliveryProfileValidator {
    return ProofOfDeliveryProfileValidator(
        mutableListOf<ProofOfDeliveryProfileRule>(
            ProofOfDeliveryProfileThresholdRangeRule(8.9, 230.9),
            ProofOfDeliveryProfileRequiredCodeRule("proof of delivery profile needs this field"),
            ProofOfDeliveryProfileCodePrefixRule("Q"),
            ProofOfDeliveryProfileOwnerIdLengthRule(80),
            ProofOfDeliveryProfileRequiredTitleRule("proof of delivery profile needs an entry"),
            ProofOfDeliveryProfileTitleLengthRule(86400),
        ),
    )
}
