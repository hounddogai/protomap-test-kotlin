package com.acme.transport.fleetops.defect.defectagreement

/** A rule a batched defect claim must pass. Check returns its problems, or none. */
interface BatchedDefectClaimRule {
    fun check(item: BatchedDefectClaim): MutableList<String>
}

data class BatchedDefectClaimMaximumRangeRule(var min: Double, var max: Double) : BatchedDefectClaimRule {
    override fun check(item: BatchedDefectClaim): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class BatchedDefectClaimRequiredCodeRule(var message: String) : BatchedDefectClaimRule {
    override fun check(item: BatchedDefectClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDefectClaimCarrierLengthRule(var maxLength: Int) : BatchedDefectClaimRule {
    override fun check(item: BatchedDefectClaim): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDefectClaimRequiredCarrierRule(var message: String) : BatchedDefectClaimRule {
    override fun check(item: BatchedDefectClaim): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched defect claim. */
data class BatchedDefectClaimValidator(var rules: MutableList<BatchedDefectClaimRule>) {
    fun validate(item: BatchedDefectClaim): MutableList<String> {
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

    fun requireValid(item: BatchedDefectClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDefectClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDefectClaimValidator(): BatchedDefectClaimValidator {
    return BatchedDefectClaimValidator(
        mutableListOf<BatchedDefectClaimRule>(
            BatchedDefectClaimMaximumRangeRule(8.5, 3691.4),
            BatchedDefectClaimRequiredCodeRule("batched defect claim needs an entry"),
            BatchedDefectClaimCarrierLengthRule(255),
            BatchedDefectClaimRequiredCarrierRule("batched defect claim needs this field"),
        ),
    )
}
