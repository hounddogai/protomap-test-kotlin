package com.acme.transport.lastmile.proofofdelivery.recorditem

/** A rule a proof of delivery batch must pass. Check returns its problems, or none. */
interface ProofOfDeliveryBatchRule {
    fun check(item: ProofOfDeliveryBatch): MutableList<String>
}

data class ProofOfDeliveryBatchCodePrefixRule(var prefix: String) : ProofOfDeliveryBatchRule {
    override fun check(item: ProofOfDeliveryBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryBatchBudgetRangeRule(var min: Double, var max: Double) : ProofOfDeliveryBatchRule {
    override fun check(item: ProofOfDeliveryBatch): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryBatchFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : ProofOfDeliveryBatchRule {
    override fun check(item: ProofOfDeliveryBatch): MutableList<String> {
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

data class ProofOfDeliveryBatchCodeLengthRule(var maxLength: Int) : ProofOfDeliveryBatchRule {
    override fun check(item: ProofOfDeliveryBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery batch. */
data class ProofOfDeliveryBatchValidator(var rules: MutableList<ProofOfDeliveryBatchRule>) {
    fun validate(item: ProofOfDeliveryBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ProofOfDeliveryBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryBatchValidator(): ProofOfDeliveryBatchValidator {
    return ProofOfDeliveryBatchValidator(
        mutableListOf<ProofOfDeliveryBatchRule>(
            ProofOfDeliveryBatchCodePrefixRule("OPS"),
            ProofOfDeliveryBatchBudgetRangeRule(4.5, 151.8),
            ProofOfDeliveryBatchFloorValueRangeRule(7.5, 3067.5),
            ProofOfDeliveryBatchCodeLengthRule(64),
        ),
    )
}
