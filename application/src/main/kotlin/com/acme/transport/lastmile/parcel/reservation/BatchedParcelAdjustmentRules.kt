package com.acme.transport.lastmile.parcel.reservation

/** A rule a batched parcel adjustment must pass. Check returns its problems, or none. */
interface BatchedParcelAdjustmentRule {
    fun check(item: BatchedParcelAdjustment): MutableList<String>
}

data class BatchedParcelAdjustmentOwnerIdLengthRule(var maxLength: Int) : BatchedParcelAdjustmentRule {
    override fun check(item: BatchedParcelAdjustment): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedParcelAdjustmentActiveGuardRule(var limit: Double) : BatchedParcelAdjustmentRule {
    override fun check(item: BatchedParcelAdjustment): MutableList<String> {
        if (item.active && item.distanceKm > limit) {
            return mutableListOf<String>("distance km ${item.distanceKm} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class BatchedParcelAdjustmentRequiredLabelRule(var message: String) : BatchedParcelAdjustmentRule {
    override fun check(item: BatchedParcelAdjustment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedParcelAdjustmentDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : BatchedParcelAdjustmentRule {
    override fun check(item: BatchedParcelAdjustment): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedParcelAdjustmentRevisionRangeRule(var min: Int, var max: Int) : BatchedParcelAdjustmentRule {
    override fun check(item: BatchedParcelAdjustment): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedParcelAdjustmentCodePrefixRule(var prefix: String) : BatchedParcelAdjustmentRule {
    override fun check(item: BatchedParcelAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched parcel adjustment. */
data class BatchedParcelAdjustmentValidator(var rules: MutableList<BatchedParcelAdjustmentRule>) {
    fun validate(item: BatchedParcelAdjustment): MutableList<String> {
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

    fun requireValid(item: BatchedParcelAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedParcelAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedParcelAdjustmentValidator(): BatchedParcelAdjustmentValidator {
    return BatchedParcelAdjustmentValidator(
        mutableListOf<BatchedParcelAdjustmentRule>(
            BatchedParcelAdjustmentOwnerIdLengthRule(3600),
            BatchedParcelAdjustmentActiveGuardRule(3.3),
            BatchedParcelAdjustmentRequiredLabelRule("batched parcel adjustment needs this field"),
            BatchedParcelAdjustmentDistanceKmRangeRule(9.1, 791.7),
            BatchedParcelAdjustmentRevisionRangeRule(255, 3600),
            BatchedParcelAdjustmentCodePrefixRule("REF"),
        ),
    )
}
