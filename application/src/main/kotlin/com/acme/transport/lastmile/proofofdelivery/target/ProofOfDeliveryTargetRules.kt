package com.acme.transport.lastmile.proofofdelivery.target

/** A rule a proof of delivery target must pass. Check returns its problems, or none. */
interface ProofOfDeliveryTargetRule {
    fun check(item: ProofOfDeliveryTarget): MutableList<String>
}

data class ProofOfDeliveryTargetDepotLengthRule(var maxLength: Int) : ProofOfDeliveryTargetRule {
    override fun check(item: ProofOfDeliveryTarget): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryTargetCodeLengthRule(var maxLength: Int) : ProofOfDeliveryTargetRule {
    override fun check(item: ProofOfDeliveryTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryTargetDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : ProofOfDeliveryTargetRule {
    override fun check(item: ProofOfDeliveryTarget): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryTargetCodePrefixRule(var prefix: String) : ProofOfDeliveryTargetRule {
    override fun check(item: ProofOfDeliveryTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryTargetLabelLengthRule(var maxLength: Int) : ProofOfDeliveryTargetRule {
    override fun check(item: ProofOfDeliveryTarget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryTargetRequiredLabelRule(var message: String) : ProofOfDeliveryTargetRule {
    override fun check(item: ProofOfDeliveryTarget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery target. */
data class ProofOfDeliveryTargetValidator(var rules: MutableList<ProofOfDeliveryTargetRule>) {
    fun validate(item: ProofOfDeliveryTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ProofOfDeliveryTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryTargetValidator(): ProofOfDeliveryTargetValidator {
    return ProofOfDeliveryTargetValidator(
        mutableListOf<ProofOfDeliveryTargetRule>(
            ProofOfDeliveryTargetDepotLengthRule(255),
            ProofOfDeliveryTargetCodeLengthRule(86400),
            ProofOfDeliveryTargetDurationMinutesRangeRule(16, 86400),
            ProofOfDeliveryTargetCodePrefixRule("Q"),
            ProofOfDeliveryTargetLabelLengthRule(120),
            ProofOfDeliveryTargetRequiredLabelRule("proof of delivery target needs an entry"),
        ),
    )
}
