package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

/** A rule a weighted dispute snapshot must pass. Check returns its problems, or none. */
interface WeightedDisputeSnapshotRule {
    fun check(item: WeightedDisputeSnapshot): MutableList<String>
}

data class WeightedDisputeSnapshotBatchSizeRangeRule(var min: Int, var max: Int) : WeightedDisputeSnapshotRule {
    override fun check(item: WeightedDisputeSnapshot): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDisputeSnapshotCodePrefixRule(var prefix: String) : WeightedDisputeSnapshotRule {
    override fun check(item: WeightedDisputeSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDisputeSnapshotRequiredExternalRefRule(var message: String) : WeightedDisputeSnapshotRule {
    override fun check(item: WeightedDisputeSnapshot): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDisputeSnapshotExternalRefLengthRule(var maxLength: Int) : WeightedDisputeSnapshotRule {
    override fun check(item: WeightedDisputeSnapshot): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted dispute snapshot. */
data class WeightedDisputeSnapshotValidator(var rules: MutableList<WeightedDisputeSnapshotRule>) {
    fun validate(item: WeightedDisputeSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedDisputeSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDisputeSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDisputeSnapshotValidator(): WeightedDisputeSnapshotValidator {
    return WeightedDisputeSnapshotValidator(
        mutableListOf<WeightedDisputeSnapshotRule>(
            WeightedDisputeSnapshotBatchSizeRangeRule(16, 120),
            WeightedDisputeSnapshotCodePrefixRule("X"),
            WeightedDisputeSnapshotRequiredExternalRefRule("weighted dispute snapshot needs this field"),
            WeightedDisputeSnapshotExternalRefLengthRule(16),
        ),
    )
}
