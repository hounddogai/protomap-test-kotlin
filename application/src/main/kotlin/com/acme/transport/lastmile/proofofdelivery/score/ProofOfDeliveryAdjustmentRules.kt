package com.acme.transport.lastmile.proofofdelivery.score

/** A rule a proof of delivery adjustment must pass. Check returns its problems, or none. */
interface ProofOfDeliveryAdjustmentRule {
    fun check(item: ProofOfDeliveryAdjustment): MutableList<String>
}

data class ProofOfDeliveryAdjustmentRequiredDescriptionRule(
    var message: String,
) : ProofOfDeliveryAdjustmentRule {
    override fun check(item: ProofOfDeliveryAdjustment): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryAdjustmentWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : ProofOfDeliveryAdjustmentRule {
    override fun check(item: ProofOfDeliveryAdjustment): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class ProofOfDeliveryAdjustmentPriorityRangeRule(
    var min: Int,
    var max: Int,
) : ProofOfDeliveryAdjustmentRule {
    override fun check(item: ProofOfDeliveryAdjustment): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryAdjustmentCodeLengthRule(var maxLength: Int) : ProofOfDeliveryAdjustmentRule {
    override fun check(item: ProofOfDeliveryAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryAdjustmentDescriptionLengthRule(var maxLength: Int) : ProofOfDeliveryAdjustmentRule {
    override fun check(item: ProofOfDeliveryAdjustment): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryAdjustmentRequiredCodeRule(var message: String) : ProofOfDeliveryAdjustmentRule {
    override fun check(item: ProofOfDeliveryAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery adjustment. */
data class ProofOfDeliveryAdjustmentValidator(var rules: MutableList<ProofOfDeliveryAdjustmentRule>) {
    fun validate(item: ProofOfDeliveryAdjustment): MutableList<String> {
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

    fun requireValid(item: ProofOfDeliveryAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryAdjustmentValidator(): ProofOfDeliveryAdjustmentValidator {
    return ProofOfDeliveryAdjustmentValidator(
        mutableListOf<ProofOfDeliveryAdjustmentRule>(
            ProofOfDeliveryAdjustmentRequiredDescriptionRule("proof of delivery adjustment needs a value"),
            ProofOfDeliveryAdjustmentWindowDaysRangeRule(80, 120),
            ProofOfDeliveryAdjustmentPriorityRangeRule(80, 1000),
            ProofOfDeliveryAdjustmentCodeLengthRule(80),
            ProofOfDeliveryAdjustmentDescriptionLengthRule(64),
            ProofOfDeliveryAdjustmentRequiredCodeRule("proof of delivery adjustment needs an entry"),
        ),
    )
}
