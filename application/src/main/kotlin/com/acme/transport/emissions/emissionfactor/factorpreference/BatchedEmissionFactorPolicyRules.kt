package com.acme.transport.emissions.emissionfactor.factorpreference

/** A rule a batched emission factor policy must pass. Check returns its problems, or none. */
interface BatchedEmissionFactorPolicyRule {
    fun check(item: BatchedEmissionFactorPolicy): MutableList<String>
}

data class BatchedEmissionFactorPolicyArchivedGuardRule(var limit: Double) : BatchedEmissionFactorPolicyRule {
    override fun check(item: BatchedEmissionFactorPolicy): MutableList<String> {
        if (item.archived && item.loadKg > limit) {
            return mutableListOf<String>("load kg ${item.loadKg} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class BatchedEmissionFactorPolicyVehicleIdLengthRule(
    var maxLength: Int,
) : BatchedEmissionFactorPolicyRule {
    override fun check(item: BatchedEmissionFactorPolicy): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedEmissionFactorPolicyCodePrefixRule(var prefix: String) : BatchedEmissionFactorPolicyRule {
    override fun check(item: BatchedEmissionFactorPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedEmissionFactorPolicyCostRangeRule(
    var min: Double,
    var max: Double,
) : BatchedEmissionFactorPolicyRule {
    override fun check(item: BatchedEmissionFactorPolicy): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched emission factor policy. */
data class BatchedEmissionFactorPolicyValidator(var rules: MutableList<BatchedEmissionFactorPolicyRule>) {
    fun validate(item: BatchedEmissionFactorPolicy): MutableList<String> {
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

    fun requireValid(item: BatchedEmissionFactorPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedEmissionFactorPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedEmissionFactorPolicyValidator(): BatchedEmissionFactorPolicyValidator {
    return BatchedEmissionFactorPolicyValidator(
        mutableListOf<BatchedEmissionFactorPolicyRule>(
            BatchedEmissionFactorPolicyArchivedGuardRule(6.3),
            BatchedEmissionFactorPolicyVehicleIdLengthRule(86400),
            BatchedEmissionFactorPolicyCodePrefixRule("OPS"),
            BatchedEmissionFactorPolicyCostRangeRule(2.3, 3107.8),
        ),
    )
}
