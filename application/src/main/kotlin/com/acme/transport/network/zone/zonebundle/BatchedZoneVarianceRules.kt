package com.acme.transport.network.zone.zonebundle

/** A rule a batched zone variance must pass. Check returns its problems, or none. */
interface BatchedZoneVarianceRule {
    fun check(item: BatchedZoneVariance): MutableList<String>
}

data class BatchedZoneVarianceCodeLengthRule(var maxLength: Int) : BatchedZoneVarianceRule {
    override fun check(item: BatchedZoneVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedZoneVarianceBatchSizeRangeRule(var min: Int, var max: Int) : BatchedZoneVarianceRule {
    override fun check(item: BatchedZoneVariance): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class BatchedZoneVarianceMinimumRangeRule(var min: Double, var max: Double) : BatchedZoneVarianceRule {
    override fun check(item: BatchedZoneVariance): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedZoneVarianceCodePrefixRule(var prefix: String) : BatchedZoneVarianceRule {
    override fun check(item: BatchedZoneVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedZoneVarianceCapacityRangeRule(var min: Int, var max: Int) : BatchedZoneVarianceRule {
    override fun check(item: BatchedZoneVariance): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class BatchedZoneVariancePhoneLengthRule(var maxLength: Int) : BatchedZoneVarianceRule {
    override fun check(item: BatchedZoneVariance): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched zone variance. */
data class BatchedZoneVarianceValidator(var rules: MutableList<BatchedZoneVarianceRule>) {
    fun validate(item: BatchedZoneVariance): MutableList<String> {
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

    fun requireValid(item: BatchedZoneVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedZoneVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedZoneVarianceValidator(): BatchedZoneVarianceValidator {
    return BatchedZoneVarianceValidator(
        mutableListOf<BatchedZoneVarianceRule>(
            BatchedZoneVarianceCodeLengthRule(255),
            BatchedZoneVarianceBatchSizeRangeRule(255, 1000),
            BatchedZoneVarianceMinimumRangeRule(1.9, 549.3),
            BatchedZoneVarianceCodePrefixRule("REF"),
            BatchedZoneVarianceCapacityRangeRule(3600, 86400),
            BatchedZoneVariancePhoneLengthRule(255),
        ),
    )
}
