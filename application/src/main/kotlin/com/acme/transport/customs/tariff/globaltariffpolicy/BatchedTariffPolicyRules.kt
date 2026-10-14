package com.acme.transport.customs.tariff.globaltariffpolicy

/** A rule a batched tariff policy must pass. Check returns its problems, or none. */
interface BatchedTariffPolicyRule {
    fun check(item: BatchedTariffPolicy): MutableList<String>
}

data class BatchedTariffPolicyVehicleIdLengthRule(var maxLength: Int) : BatchedTariffPolicyRule {
    override fun check(item: BatchedTariffPolicy): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTariffPolicyPalletsRangeRule(var min: Int, var max: Int) : BatchedTariffPolicyRule {
    override fun check(item: BatchedTariffPolicy): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class BatchedTariffPolicyCreatedAtRangeRule(var min: Int, var max: Int) : BatchedTariffPolicyRule {
    override fun check(item: BatchedTariffPolicy): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTariffPolicyCodeLengthRule(var maxLength: Int) : BatchedTariffPolicyRule {
    override fun check(item: BatchedTariffPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTariffPolicyAmountRangeRule(var min: Double, var max: Double) : BatchedTariffPolicyRule {
    override fun check(item: BatchedTariffPolicy): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched tariff policy. */
data class BatchedTariffPolicyValidator(var rules: MutableList<BatchedTariffPolicyRule>) {
    fun validate(item: BatchedTariffPolicy): MutableList<String> {
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

    fun requireValid(item: BatchedTariffPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTariffPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTariffPolicyValidator(): BatchedTariffPolicyValidator {
    return BatchedTariffPolicyValidator(
        mutableListOf<BatchedTariffPolicyRule>(
            BatchedTariffPolicyVehicleIdLengthRule(255),
            BatchedTariffPolicyPalletsRangeRule(32, 255),
            BatchedTariffPolicyCreatedAtRangeRule(80, 1000),
            BatchedTariffPolicyCodeLengthRule(80),
            BatchedTariffPolicyAmountRangeRule(0.2, 4013.1),
        ),
    )
}
